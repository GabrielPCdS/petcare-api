package com.petcareconnect.api.aviso;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcareconnect.api.common.NegocioException;
import com.petcareconnect.api.common.RecursoNaoEncontradoException;
import com.petcareconnect.api.pet.Pet;
import com.petcareconnect.api.pet.PetRepository;
import com.petcareconnect.api.security.UsuarioLogado;
import com.petcareconnect.api.usuario.Usuario;
import com.petcareconnect.api.usuario.UsuarioRepository;

@Service
public class AvisoService {

    private static final String MSG_NAO_ENCONTRADO = "Aviso não encontrado.";
    private static final String MSG_VET_SEM_ACESSO =
            "Você só pode enviar avisos a tutores dos pets que atende.";
    private static final Set<String> TIPOS =
            Set.of(Aviso.GERAL, Aviso.CONSULTA, Aviso.PET, Aviso.TUTOR, Aviso.VET);

    private final AvisoRepository avisos;
    private final AvisoLeituraRepository leituras;
    private final UsuarioRepository usuarios;
    private final PetRepository pets;

    public AvisoService(AvisoRepository avisos, AvisoLeituraRepository leituras,
                        UsuarioRepository usuarios, PetRepository pets) {
        this.avisos = avisos;
        this.leituras = leituras;
        this.usuarios = usuarios;
        this.pets = pets;
    }

    // Aviso gerado pelo servidor (pet cadastrado, consulta agendada, etc.)
    @Transactional
    public void criarAutomatico(String titulo, String texto, String tipo, Long tutorId,
                                Long petId, Long veterinarioId, Long autorId) {
        Aviso a = new Aviso();
        a.setTitulo(titulo);
        a.setTexto(texto);
        a.setTipo(tipo);
        a.setTutorId(tutorId);
        a.setPetId(petId);
        a.setVeterinarioId(veterinarioId);
        a.setAutorId(autorId);
        avisos.save(a);
    }

    @Transactional(readOnly = true)
    public List<AvisoResponse> listar(UsuarioLogado u) {
        List<Aviso> lista = switch (u.perfil()) {
            case Usuario.TUTOR -> avisos.findVisiveisParaTutor(u.id());
            case Usuario.VETERINARIO -> avisos.findVisiveisParaVeterinario(u.id());
            default -> avisos.findAllByOrderByCriadoEmDesc();
        };
        Set<Long> lidos = leituras.findAvisoIdsLidosPor(u.id());
        return lista.stream()
                .sorted(Comparator.comparing(Aviso::getCriadoEm).reversed()
                        .thenComparing(Aviso::getId, Comparator.reverseOrder()))
                .map(a -> AvisoResponse.de(a, lidos.contains(a.getId())))
                .toList();
    }

    @Transactional
    public Long criar(AvisoRequest req, UsuarioLogado u) {
        Aviso a = new Aviso();
        preencher(a, req, u);
        a.setAutorId(u.id());
        return avisos.save(a).getId();
    }

    @Transactional
    public void atualizar(Long id, AvisoRequest req, UsuarioLogado u) {
        Aviso a = avisos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MSG_NAO_ENCONTRADO));
        preencher(a, req, u);
        avisos.save(a);
    }

    @Transactional
    public void apagar(Long id) {
        Aviso a = avisos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MSG_NAO_ENCONTRADO));
        avisos.delete(a);
    }

    // Marca como lido para o usuario logado, somente se ele puder ver o aviso
    @Transactional
    public void marcarLido(Long id, UsuarioLogado u) {
        Aviso a = avisos.findById(id)
                .filter(x -> visivelPara(x, u))
                .orElseThrow(() -> new RecursoNaoEncontradoException(MSG_NAO_ENCONTRADO));
        leituras.marcarLido(a.getId(), u.id());
    }

    // ---------- auxiliares ----------

    private boolean visivelPara(Aviso a, UsuarioLogado u) {
        return switch (u.perfil()) {
            case Usuario.ADMIN -> true;
            case Usuario.TUTOR -> Aviso.GERAL.equals(a.getTipo()) || u.id().equals(a.getTutorId());
            case Usuario.VETERINARIO -> Aviso.GERAL.equals(a.getTipo())
                    || (Aviso.VET.equals(a.getTipo()) && u.id().equals(a.getVeterinarioId()))
                    || u.id().equals(a.getAutorId());
            default -> false;
        };
    }

    private void preencher(Aviso a, AvisoRequest req, UsuarioLogado u) {
        String tipo = req.tipo().trim().toLowerCase();
        if (!TIPOS.contains(tipo)) {
            throw new NegocioException("Tipo inválido. Use geral, consulta, pet, tutor ou vet.");
        }

        boolean ehVet = Usuario.VETERINARIO.equals(u.perfil());
        Long tutorId = req.tutorId();
        Long petId = req.petId();
        Long vetId = req.veterinarioId();

        if (ehVet && (Aviso.GERAL.equals(tipo) || Aviso.VET.equals(tipo))) {
            throw new NegocioException("Veterinários só podem enviar avisos a tutores.");
        }

        if (Aviso.GERAL.equals(tipo)) {
            tutorId = null;
            petId = null;
            vetId = null;
        } else {
            if ((ehVet || Aviso.TUTOR.equals(tipo) || Aviso.PET.equals(tipo)) && tutorId == null) {
                throw new NegocioException("Informe o tutor que receberá o aviso.");
            }
            if (Aviso.PET.equals(tipo) && petId == null) {
                throw new NegocioException("Informe o pet do aviso.");
            }
            if (Aviso.VET.equals(tipo) && vetId == null) {
                throw new NegocioException("Informe o veterinário que receberá o aviso.");
            }
        }

        if (tutorId != null) {
            usuarios.findById(tutorId)
                    .filter(t -> Usuario.TUTOR.equals(t.getPerfil()))
                    .orElseThrow(() -> new NegocioException("Tutor não encontrado."));
            if (ehVet && !pets.veterinarioAtendeTutor(tutorId, u.id())) {
                throw new NegocioException(MSG_VET_SEM_ACESSO);
            }
        }

        if (petId != null) {
            if (tutorId == null) {
                throw new NegocioException("Informe o tutor ao informar um pet.");
            }
            Pet pet = pets.findById(petId)
                    .orElseThrow(() -> new NegocioException("Pet não encontrado."));
            if (!pet.getTutor().getId().equals(tutorId)) {
                throw new NegocioException("O pet não pertence ao tutor informado.");
            }
            if (ehVet && !pets.veterinarioAtende(petId, u.id())) {
                throw new NegocioException(MSG_VET_SEM_ACESSO);
            }
        }

        if (vetId != null) {
            usuarios.findById(vetId)
                    .filter(v -> Usuario.VETERINARIO.equals(v.getPerfil()) && v.isAtivo())
                    .orElseThrow(() -> new NegocioException("Veterinário não encontrado."));
        }

        a.setTitulo(req.titulo().trim());
        a.setTexto(req.texto().trim());
        a.setTipo(tipo);
        a.setTutorId(tutorId);
        a.setPetId(petId);
        a.setVeterinarioId(vetId);
    }
}