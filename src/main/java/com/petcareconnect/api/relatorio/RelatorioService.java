package com.petcareconnect.api.relatorio;

import java.util.Comparator;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcareconnect.api.common.NegocioException;
import com.petcareconnect.api.consulta.Consulta;
import com.petcareconnect.api.consulta.ConsultaRepository;
import com.petcareconnect.api.pet.Pet;
import com.petcareconnect.api.pet.PetService;
import com.petcareconnect.api.security.UsuarioLogado;
import com.petcareconnect.api.usuario.Usuario;

@Service
public class RelatorioService {

    private static final String MSG_RN003 =
            "O pet deve ter ao menos uma consulta com status \"Realizada\" para gerar relatório (Regra RN-003).";

    private final RelatorioRepository relatorios;
    private final ConsultaRepository consultas;
    private final PetService pets;

    public RelatorioService(RelatorioRepository relatorios, ConsultaRepository consultas, PetService pets) {
        this.relatorios = relatorios;
        this.consultas = consultas;
        this.pets = pets;
    }

    @Transactional(readOnly = true)
    public List<RelatorioResponse> listar(Long petId, UsuarioLogado u) {
        List<Relatorio> lista;
        if (petId != null) {
            pets.buscarComAcesso(petId, u);
            lista = relatorios.findByPetId(petId).map(List::of).orElse(List.of());
        } else {
            lista = switch (u.perfil()) {
                case Usuario.TUTOR -> relatorios.findByPetTutorId(u.id());
                case Usuario.VETERINARIO -> relatorios.findDosPetsAtendidosPor(u.id());
                default -> relatorios.findAll();
            };
        }
        return lista.stream()
                .sorted(Comparator.comparing(Relatorio::getId))
                .map(RelatorioResponse::de)
                .toList();
    }

    // Um relatorio por pet: cria ou atualiza. Retorna o id.
    @Transactional
    public Long salvar(RelatorioCriarRequest req, UsuarioLogado u) {
        Pet pet = pets.buscarComAcesso(req.petId(), u);

        // RN-003
        if (!consultas.existsByPetIdAndStatus(pet.getId(), Consulta.REALIZADA)) {
            throw new NegocioException(MSG_RN003);
        }

        Relatorio r = relatorios.findByPetId(pet.getId()).orElseGet(() -> {
            Relatorio novo = new Relatorio();
            novo.setPet(pet);
            return novo;
        });
        r.setTexto(req.texto().trim());
        r.setStatus(Relatorio.DISPONIVEL);

        try {
            return relatorios.saveAndFlush(r).getId();
        } catch (DataIntegrityViolationException e) {
            // Duas requisicoes simultaneas criando o primeiro relatorio do pet
            throw new NegocioException("O relatório deste pet foi criado agora por outra requisição. Tente novamente.");
        }
    }
}