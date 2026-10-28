package com.petcareconnect.api.consulta;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcareconnect.api.aviso.Aviso;
import com.petcareconnect.api.aviso.AvisoService;
import com.petcareconnect.api.common.Formatos;
import com.petcareconnect.api.common.NegocioException;
import com.petcareconnect.api.common.RecursoNaoEncontradoException;
import com.petcareconnect.api.pet.Pet;
import com.petcareconnect.api.pet.PetService;
import com.petcareconnect.api.security.UsuarioLogado;
import com.petcareconnect.api.usuario.Usuario;
import com.petcareconnect.api.usuario.UsuarioRepository;

@Service
public class ConsultaService {

    private static final String MSG_RN002 = "Horário já reservado para este veterinário (Regra RN-002).";
    private static final String MSG_NAO_ENCONTRADA = "Consulta não encontrada.";
    private static final String MSG_VET_INVALIDO = "Veterinário não encontrado.";

    private static final DateTimeFormatter DATA_ESTRITA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final Set<String> STATUS_VALIDOS = Set.of(
            Consulta.MARCADA, Consulta.REALIZADA, Consulta.CANCELADA, Consulta.NAO_COMPARECEU);

    private final ConsultaRepository consultas;
    private final UsuarioRepository usuarios;
    private final PetService pets;
    private final AvisoService avisos;
    private final Clock clock;
    private final LocalTime inicio;
    private final LocalTime fim;
    private final int intervalo;

    public ConsultaService(ConsultaRepository consultas, UsuarioRepository usuarios, PetService pets,
                           AvisoService avisos, Clock clock,
                           @Value("${app.agenda.inicio}") String inicio,
                           @Value("${app.agenda.fim}") String fim,
                           @Value("${app.agenda.intervalo-minutos}") int intervalo) {
        if (intervalo <= 0) {
            throw new IllegalStateException("app.agenda.intervalo-minutos deve ser maior que zero.");
        }
        this.consultas = consultas;
        this.usuarios = usuarios;
        this.pets = pets;
        this.avisos = avisos;
        this.clock = clock;
        this.inicio = LocalTime.parse(inicio);
        this.fim = LocalTime.parse(fim);
        this.intervalo = intervalo;
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> listar(UsuarioLogado u) {
        List<Consulta> lista = switch (u.perfil()) {
            case Usuario.TUTOR -> consultas.findByPetTutorId(u.id());
            case Usuario.VETERINARIO -> consultas.findByVeterinarioId(u.id());
            default -> consultas.findAll();
        };
        return lista.stream()
                .sorted(Comparator.comparing(Consulta::getData)
                        .thenComparing(Consulta::getHora)
                        .thenComparing(Consulta::getId))
                .map(ConsultaResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> horariosDisponiveis(Long veterinarioId, String dataTexto) {
        LocalDate data = lerData(dataTexto);
        veterinarioValido(veterinarioId);

        LocalDateTime agora = LocalDateTime.now(clock);
        if (data.isBefore(agora.toLocalDate())) {
            return List.of();
        }

        Set<LocalTime> ocupados = consultas
                .findByVeterinarioIdAndDataAndStatusNot(veterinarioId, data, Consulta.CANCELADA)
                .stream().map(Consulta::getHora).collect(Collectors.toSet());

        return grade().stream()
                .filter(h -> !ocupados.contains(h))
                .filter(h -> LocalDateTime.of(data, h).isAfter(agora))
                .map(h -> h.format(Formatos.HORA))
                .toList();
    }

    @Transactional
    public Long criar(ConsultaCriarRequest req, UsuarioLogado u) {
        Pet pet = pets.buscarComAcesso(req.petId(), u);
        Usuario vet = veterinarioValido(req.veterinarioId());
        LocalDate data = lerData(req.data());
        LocalTime hora = lerHora(req.hora());

        if (!LocalDateTime.of(data, hora).isAfter(LocalDateTime.now(clock))) {
            throw new NegocioException("Só é possível agendar para data e hora futuras.");
        }
        if (!grade().contains(hora)) {
            throw new NegocioException("Horário fora do período de atendimento.");
        }
        if (consultas.existsByVeterinarioIdAndDataAndHoraAndStatusNot(
                vet.getId(), data, hora, Consulta.CANCELADA)) {
            throw new NegocioException(MSG_RN002);
        }

        Consulta c = new Consulta();
        c.setPet(pet);
        c.setVeterinario(vet);
        c.setData(data);
        c.setHora(hora);
        c.setStatus(Consulta.MARCADA);
        c.setObservacoes(vazioParaNulo(req.observacoes()));
        try {
            c = consultas.saveAndFlush(c);
        } catch (DataIntegrityViolationException e) {
            // Duas requisicoes simultaneas: o indice unico do banco segura a segunda
            throw new NegocioException(MSG_RN002);
        }

        avisar("Consulta agendada", descricao(c) + " foi agendada.", c, u);
        return c.getId();
    }

    // RN-005: menos de 24h vira NaoCompareceu, senao Cancelada. Retorna o status final.
    @Transactional
    public String cancelar(Long id, UsuarioLogado u) {
        Consulta c = buscarParaCancelar(id, u);
        if (!Consulta.MARCADA.equals(c.getStatus())) {
            throw new NegocioException("Só é possível cancelar consultas com status Marcada.");
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        Duration antecedencia = Duration.between(agora, LocalDateTime.of(c.getData(), c.getHora()));
        boolean tardio = antecedencia.compareTo(Duration.ofHours(24)) < 0;

        c.setStatus(tardio ? Consulta.NAO_COMPARECEU : Consulta.CANCELADA);
        consultas.save(c);

        String texto = descricao(c) + (tardio
                ? " foi cancelada com menos de 24h de antecedência e registrada como não comparecimento."
                : " foi cancelada.");
        avisar("Consulta cancelada", texto, c, u);
        return c.getStatus();
    }

    @Transactional
    public void atualizarStatus(Long id, String status, String obs, UsuarioLogado u) {
        String novo = status == null ? "" : status.trim();
        if (!STATUS_VALIDOS.contains(novo)) {
            throw new NegocioException("Status inválido. Use Marcada, Realizada, Cancelada ou NaoCompareceu.");
        }

        Consulta c = buscarParaStatus(id, u);
        if (!Consulta.MARCADA.equals(c.getStatus())) {
            throw new NegocioException("Só é possível alterar o status de consultas Marcadas.");
        }
        if (Consulta.MARCADA.equals(novo)) {
            throw new NegocioException("A consulta já está com status Marcada.");
        }

        c.setStatus(novo);
        if (obs != null && !obs.isBlank()) {
            c.setObservacoes(obs.trim());
        }
        consultas.save(c);

        if (Consulta.REALIZADA.equals(novo)) {
            avisar("Consulta concluída", descricao(c) + " foi concluída.", c, u);
        } else if (Consulta.CANCELADA.equals(novo)) {
            avisar("Consulta cancelada", descricao(c) + " foi cancelada.", c, u);
        }
    }

    // ---------- auxiliares ----------

    private List<LocalTime> grade() {
        List<LocalTime> grade = new ArrayList<>();
        LocalTime h = inicio;
        while (!h.isAfter(fim)) {
            grade.add(h);
            LocalTime proximo = h.plusMinutes(intervalo);
            if (!proximo.isAfter(h)) {
                break; // passou da meia-noite
            }
            h = proximo;
        }
        return grade;
    }

    private Usuario veterinarioValido(Long id) {
        return usuarios.findById(id)
                .filter(v -> Usuario.VETERINARIO.equals(v.getPerfil()) && v.isAtivo())
                .orElseThrow(() -> new NegocioException(MSG_VET_INVALIDO));
    }

    private Consulta buscarParaCancelar(Long id, UsuarioLogado u) {
        Consulta c = consultas.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MSG_NAO_ENCONTRADA));
        boolean pode = Usuario.ADMIN.equals(u.perfil())
                || (Usuario.TUTOR.equals(u.perfil()) && c.getPet().getTutor().getId().equals(u.id()));
        if (!pode) {
            throw new RecursoNaoEncontradoException(MSG_NAO_ENCONTRADA);
        }
        return c;
    }

    private Consulta buscarParaStatus(Long id, UsuarioLogado u) {
        Consulta c = consultas.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MSG_NAO_ENCONTRADA));
        boolean pode = Usuario.ADMIN.equals(u.perfil())
                || (Usuario.VETERINARIO.equals(u.perfil()) && c.getVeterinario().getId().equals(u.id()));
        if (!pode) {
            throw new RecursoNaoEncontradoException(MSG_NAO_ENCONTRADA);
        }
        return c;
    }

    private void avisar(String titulo, String texto, Consulta c, UsuarioLogado u) {
        avisos.criarAutomatico(titulo, texto, Aviso.CONSULTA, c.getPet().getTutor().getId(),
                c.getPet().getId(), c.getVeterinario().getId(), u.id());
    }

    private String descricao(Consulta c) {
        return "A consulta de " + c.getPet().getNome() + " com " + c.getVeterinario().getNome()
                + " em " + c.getData().format(Formatos.DATA)
                + " às " + c.getHora().format(Formatos.HORA);
    }

    private LocalDate lerData(String texto) {
        try {
            return LocalDate.parse(texto.trim(), DATA_ESTRITA);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new NegocioException("Data inválida. Use o formato dd/MM/yyyy.");
        }
    }

    private LocalTime lerHora(String texto) {
        try {
            return LocalTime.parse(texto.trim(), Formatos.HORA);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new NegocioException("Hora inválida. Use o formato HH:mm.");
        }
    }

    private String vazioParaNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}