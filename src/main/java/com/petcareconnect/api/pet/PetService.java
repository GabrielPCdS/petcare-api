package com.petcareconnect.api.pet;

import java.util.Comparator;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcareconnect.api.aviso.Aviso;
import com.petcareconnect.api.aviso.AvisoService;
import com.petcareconnect.api.common.NegocioException;
import com.petcareconnect.api.common.RecursoNaoEncontradoException;
import com.petcareconnect.api.security.UsuarioLogado;
import com.petcareconnect.api.usuario.Usuario;
import com.petcareconnect.api.usuario.UsuarioRepository;

@Service
public class PetService {

    private static final String MSG_NAO_ENCONTRADO = "Pet não encontrado.";
    private static final String MSG_DUPLICADO = "Este tutor já possui um pet com este nome.";

    private final PetRepository pets;
    private final UsuarioRepository usuarios;
    private final AvisoService avisos;

    public PetService(PetRepository pets, UsuarioRepository usuarios, AvisoService avisos) {
        this.pets = pets;
        this.usuarios = usuarios;
        this.avisos = avisos;
    }

    @Transactional(readOnly = true)
    public List<PetResponse> listar(UsuarioLogado u) {
        List<Pet> lista = switch (u.perfil()) {
            case Usuario.TUTOR -> pets.findByTutorId(u.id());
            case Usuario.VETERINARIO -> pets.findAtendidosPeloVeterinario(u.id());
            default -> pets.findAll();
        };
        return lista.stream()
                .sorted(Comparator.comparing(Pet::getId))
                .map(PetResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public PetResponse obter(Long id, UsuarioLogado u) {
        return PetResponse.de(buscarComAcesso(id, u));
    }

    @Transactional
    public Long criar(PetCriarRequest req, UsuarioLogado u) {
        Usuario tutor;
        if (Usuario.TUTOR.equals(u.perfil())) {
            tutor = usuarios.findById(u.id())
                    .orElseThrow(() -> new NegocioException("Usuário não encontrado."));
        } else {
            if (req.tutorId() == null) {
                throw new NegocioException("Informe o tutor do pet.");
            }
            tutor = tutorValido(req.tutorId());
        }

        String nome = req.nome().trim();
        if (pets.existsByTutorIdAndNomeIgnoreCase(tutor.getId(), nome)) {
            throw new NegocioException(MSG_DUPLICADO);
        }

        Pet p = new Pet();
        p.setNome(nome);
        p.setTipo(req.tipo().trim());
        p.setRaca(vazioParaNulo(req.raca()));
        p.setIdade(req.idade());
        p.setPeso(req.peso());
        p.setTutor(tutor);
        p = salvar(p);

        avisos.criarAutomatico("Pet cadastrado",
                "O pet " + p.getNome() + " foi cadastrado com sucesso.",
                Aviso.PET, tutor.getId(), p.getId(), null, u.id());
        return p.getId();
    }

    @Transactional
    public void atualizar(Long id, PetAtualizarRequest req, UsuarioLogado u) {
        Pet pet = buscarParaAlterar(id, u);

        Usuario tutorFinal = pet.getTutor();
        if (Usuario.ADMIN.equals(u.perfil()) && req.tutorId() != null
                && !req.tutorId().equals(tutorFinal.getId())) {
            tutorFinal = tutorValido(req.tutorId());
        }

        String nomeFinal = pet.getNome();
        if (req.nome() != null) {
            nomeFinal = req.nome().trim();
            if (nomeFinal.isEmpty()) {
                throw new NegocioException("Informe o nome do pet.");
            }
        }
        if (req.tipo() != null && req.tipo().isBlank()) {
            throw new NegocioException("Informe o tipo do pet.");
        }

        if (pets.existsByTutorIdAndNomeIgnoreCaseAndIdNot(tutorFinal.getId(), nomeFinal, pet.getId())) {
            throw new NegocioException(MSG_DUPLICADO);
        }

        pet.setNome(nomeFinal);
        pet.setTutor(tutorFinal);
        if (req.tipo() != null) pet.setTipo(req.tipo().trim());
        if (req.raca() != null) pet.setRaca(vazioParaNulo(req.raca()));
        if (req.idade() != null) pet.setIdade(req.idade());
        if (req.peso() != null) pet.setPeso(req.peso());
        salvar(pet);
    }

    @Transactional
    public void remover(Long id, UsuarioLogado u) {
        pets.delete(buscarParaAlterar(id, u));
    }

    @Transactional(readOnly = true)
    public Pet buscarComAcesso(Long id, UsuarioLogado u) {
        Pet pet = pets.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MSG_NAO_ENCONTRADO));
        boolean pode = switch (u.perfil()) {
            case Usuario.ADMIN -> true;
            case Usuario.TUTOR -> pet.getTutor().getId().equals(u.id());
            case Usuario.VETERINARIO -> pets.veterinarioAtende(pet.getId(), u.id());
            default -> false;
        };
        if (!pode) {
            throw new RecursoNaoEncontradoException(MSG_NAO_ENCONTRADO);
        }
        return pet;
    }

    private Pet buscarParaAlterar(Long id, UsuarioLogado u) {
        Pet pet = pets.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MSG_NAO_ENCONTRADO));
        boolean pode = Usuario.ADMIN.equals(u.perfil())
                || (Usuario.TUTOR.equals(u.perfil()) && pet.getTutor().getId().equals(u.id()));
        if (!pode) {
            throw new RecursoNaoEncontradoException(MSG_NAO_ENCONTRADO);
        }
        return pet;
    }

    private Usuario tutorValido(Long id) {
        return usuarios.findById(id)
                .filter(t -> Usuario.TUTOR.equals(t.getPerfil()))
                .orElseThrow(() -> new NegocioException("Tutor não encontrado."));
    }

    private Pet salvar(Pet p) {
        try {
            return pets.saveAndFlush(p);
        } catch (DataIntegrityViolationException e) {
            throw new NegocioException(MSG_DUPLICADO);
        }
    }

    private String vazioParaNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}