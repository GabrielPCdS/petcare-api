package com.petcareconnect.api.vacina;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcareconnect.api.common.Datas;
import com.petcareconnect.api.common.NegocioException;
import com.petcareconnect.api.common.RecursoNaoEncontradoException;
import com.petcareconnect.api.pet.Pet;
import com.petcareconnect.api.pet.PetService;
import com.petcareconnect.api.security.UsuarioLogado;
import com.petcareconnect.api.usuario.Usuario;

@Service
public class VacinaService {

    private static final String MSG_NAO_ENCONTRADA = "Vacina não encontrada.";

    private final VacinaRepository vacinas;
    private final PetService pets;
    private final Clock clock;

    public VacinaService(VacinaRepository vacinas, PetService pets, Clock clock) {
        this.vacinas = vacinas;
        this.pets = pets;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<VacinaResponse> listarDoPet(Long petId, UsuarioLogado u) {
        pets.buscarComAcesso(petId, u);
        return paraResposta(vacinas.findByPetId(petId));
    }

    @Transactional(readOnly = true)
    public List<VacinaResponse> listar(UsuarioLogado u) {
        List<Vacina> lista = switch (u.perfil()) {
            case Usuario.TUTOR -> vacinas.findByPetTutorId(u.id());
            case Usuario.VETERINARIO -> vacinas.findDosPetsAtendidosPor(u.id());
            default -> vacinas.findAll();
        };
        return paraResposta(lista);
    }

    @Transactional
    public Long criar(VacinaCriarRequest req, UsuarioLogado u) {
        Pet pet = pets.buscarComAcesso(req.petId(), u);
        LocalDate hoje = LocalDate.now(clock);
        boolean aplicada = Boolean.TRUE.equals(req.aplicada());

        LocalDate data = (req.dataAplicacao() == null || req.dataAplicacao().isBlank())
                ? null : Datas.ler(req.dataAplicacao());
        if (aplicada && data == null) {
            data = hoje;
        }
        if (aplicada && data.isAfter(hoje)) {
            throw new NegocioException("A data de aplicação não pode ser futura.");
        }

        Vacina v = new Vacina();
        v.setPet(pet);
        v.setNome(req.nome().trim());
        v.setDataAplicacao(data);
        v.setObservacao(vazioParaNulo(req.observacao()));
        v.setAplicada(aplicada);
        return vacinas.save(v).getId();
    }

    @Transactional
    public void aplicar(Long id, String dataTexto, String obs, UsuarioLogado u) {
        Vacina v = vacinas.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MSG_NAO_ENCONTRADA));
        try {
            pets.buscarComAcesso(v.getPet().getId(), u);
        } catch (RecursoNaoEncontradoException e) {
            throw new RecursoNaoEncontradoException(MSG_NAO_ENCONTRADA);
        }
        if (v.isAplicada()) {
            throw new NegocioException("Esta vacina já foi aplicada.");
        }

        LocalDate hoje = LocalDate.now(clock);
        LocalDate data = (dataTexto == null || dataTexto.isBlank()) ? hoje : Datas.ler(dataTexto);
        if (data.isAfter(hoje)) {
            throw new NegocioException("A data de aplicação não pode ser futura.");
        }

        v.setAplicada(true);
        v.setDataAplicacao(data);
        if (obs != null && !obs.isBlank()) {
            v.setObservacao(obs.trim());
        }
        vacinas.save(v);
    }

    private List<VacinaResponse> paraResposta(List<Vacina> lista) {
        return lista.stream()
                .sorted(Comparator.comparing(Vacina::getId))
                .map(VacinaResponse::de)
                .toList();
    }

    private String vazioParaNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}