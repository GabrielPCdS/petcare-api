package com.petcareconnect.api.historico;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcareconnect.api.common.Datas;
import com.petcareconnect.api.pet.Pet;
import com.petcareconnect.api.pet.PetService;
import com.petcareconnect.api.security.UsuarioLogado;

@Service
public class HistoricoService {

    private final HistoricoRepository historicos;
    private final PetService pets;

    public HistoricoService(HistoricoRepository historicos, PetService pets) {
        this.historicos = historicos;
        this.pets = pets;
    }

    @Transactional(readOnly = true)
    public List<HistoricoResponse> listar(Long petId, UsuarioLogado u) {
        pets.buscarComAcesso(petId, u);
        return historicos.findByPetIdOrderByDataEventoDesc(petId).stream()
                .map(HistoricoResponse::de)
                .toList();
    }

    @Transactional
    public Long criar(HistoricoCriarRequest req, UsuarioLogado u) {
        Pet pet = pets.buscarComAcesso(req.petId(), u);

        Historico h = new Historico();
        h.setPet(pet);
        h.setDescricao(req.descricao().trim());
        h.setDataEvento(Datas.ler(req.dataEvento()));
        return historicos.save(h).getId();
    }
}