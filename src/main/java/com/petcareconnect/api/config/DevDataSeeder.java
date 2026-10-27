package com.petcareconnect.api.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.petcareconnect.api.aviso.Aviso;
import com.petcareconnect.api.aviso.AvisoRepository;
import com.petcareconnect.api.consulta.Consulta;
import com.petcareconnect.api.consulta.ConsultaRepository;
import com.petcareconnect.api.historico.Historico;
import com.petcareconnect.api.historico.HistoricoRepository;
import com.petcareconnect.api.pet.Pet;
import com.petcareconnect.api.pet.PetRepository;
import com.petcareconnect.api.relatorio.Relatorio;
import com.petcareconnect.api.relatorio.RelatorioRepository;
import com.petcareconnect.api.usuario.Usuario;
import com.petcareconnect.api.usuario.UsuarioRepository;
import com.petcareconnect.api.vacina.Vacina;
import com.petcareconnect.api.vacina.VacinaRepository;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final UsuarioRepository usuarios;
    private final PetRepository pets;
    private final ConsultaRepository consultas;
    private final VacinaRepository vacinas;
    private final HistoricoRepository historicos;
    private final RelatorioRepository relatorios;
    private final AvisoRepository avisos;
    private final PasswordEncoder encoder;

    public DevDataSeeder(UsuarioRepository usuarios, PetRepository pets, ConsultaRepository consultas,
                         VacinaRepository vacinas, HistoricoRepository historicos,
                         RelatorioRepository relatorios, AvisoRepository avisos,
                         PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.pets = pets;
        this.consultas = consultas;
        this.vacinas = vacinas;
        this.historicos = historicos;
        this.relatorios = relatorios;
        this.avisos = avisos;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarios.count() > 0) {
            log.info("Seed ignorado: o banco ja tem usuarios.");
            return;
        }

        LocalDate hoje = LocalDate.now();

        Usuario maria = criarUsuario("Maria Oliveira", "tutor_teste", "Tutor123!", Usuario.TUTOR,
                "maria@petcare.com", "(11) 91111-1111", "Rua das Flores, 100", "Ativo", null);
        Usuario carlos = criarUsuario("Dr. Carlos Mendes", "vet_teste", "Vet123!", Usuario.VETERINARIO,
                "carlos@petcare.com", "(11) 92222-2222", null, null, "CRMV-SP 12345");
        Usuario admin = criarUsuario("Administrador PetCare", "admin_teste", "Admin123!", Usuario.ADMIN,
                "admin@petcare.com", "(11) 93333-3333", null, null, null);

        Pet rex = criarPet("Rex", "Cachorro", "Labrador Retriever", 3, "25.50", maria);
        Pet mia = criarPet("Mia", "Gato", "Siames", 2, "4.20", maria);

        criarConsulta(rex, carlos, hoje.minusDays(10), LocalTime.of(9, 0), Consulta.REALIZADA,
                "Check-up anual sem alteracoes.");
        criarConsulta(mia, carlos, hoje.plusDays(3), LocalTime.of(10, 0), Consulta.MARCADA,
                "Revisao e vacina.");

        criarVacina(rex, "V10", hoje.minusMonths(6), "Dose anual aplicada.", true);
        criarVacina(rex, "Antirrabica", hoje.minusMonths(6), null, true);
        criarVacina(mia, "V4 Felina", null, "Aguardando aplicacao.", false);

        criarHistorico(rex, "Primeira consulta e avaliacao geral.", hoje.minusMonths(8));
        criarHistorico(rex, "Vermifugacao realizada.", hoje.minusMonths(6));
        criarHistorico(mia, "Cadastro no sistema e avaliacao inicial.", hoje.minusMonths(2));

        criarRelatorio(rex, "Animal saudavel, peso adequado, vacinas em dia.");
        criarRelatorio(mia, "Animal ativo e saudavel. Vacina V4 pendente.");

        criarAviso("Bem-vindo ao PetCare Connect", "Estamos felizes em cuidar do seu pet.",
                Aviso.GERAL, null, null, null, admin.getId());
        criarAviso("Consulta agendada", "A consulta da Mia foi marcada.",
                Aviso.CONSULTA, maria.getId(), mia.getId(), carlos.getId(), carlos.getId());
        criarAviso("Vacina pendente", "A Mia precisa tomar a vacina V4 Felina.",
                Aviso.PET, maria.getId(), mia.getId(), null, carlos.getId());

        log.info("Seed de desenvolvimento criado: 3 usuarios, 2 pets, 2 consultas, 3 vacinas, "
                + "3 historicos, 2 relatorios e 3 avisos.");
    }

    private Usuario criarUsuario(String nome, String login, String senha, String perfil, String email,
                                 String telefone, String endereco, String convenio, String registro) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setLogin(login);
        u.setSenhaHash(encoder.encode(senha));
        u.setPerfil(perfil);
        u.setEmail(email);
        u.setTelefone(telefone);
        u.setEndereco(endereco);
        u.setConvenioStatus(convenio);
        u.setRegistro(registro);
        u.setAtivo(true);
        return usuarios.save(u);
    }

    private Pet criarPet(String nome, String tipo, String raca, int idade, String peso, Usuario tutor) {
        Pet p = new Pet();
        p.setNome(nome);
        p.setTipo(tipo);
        p.setRaca(raca);
        p.setIdade(idade);
        p.setPeso(new BigDecimal(peso));
        p.setTutor(tutor);
        return pets.save(p);
    }

    private void criarConsulta(Pet pet, Usuario vet, LocalDate data, LocalTime hora, String status,
                               String obs) {
        Consulta c = new Consulta();
        c.setPet(pet);
        c.setVeterinario(vet);
        c.setData(data);
        c.setHora(hora);
        c.setStatus(status);
        c.setObservacoes(obs);
        consultas.save(c);
    }

    private void criarVacina(Pet pet, String nome, LocalDate data, String obs, boolean aplicada) {
        Vacina v = new Vacina();
        v.setPet(pet);
        v.setNome(nome);
        v.setDataAplicacao(data);
        v.setObservacao(obs);
        v.setAplicada(aplicada);
        vacinas.save(v);
    }

    private void criarHistorico(Pet pet, String descricao, LocalDate data) {
        Historico h = new Historico();
        h.setPet(pet);
        h.setDescricao(descricao);
        h.setDataEvento(data);
        historicos.save(h);
    }

    private void criarRelatorio(Pet pet, String texto) {
        Relatorio r = new Relatorio();
        r.setPet(pet);
        r.setTexto(texto);
        relatorios.save(r);
    }

    private void criarAviso(String titulo, String texto, String tipo, Long tutorId, Long petId,
                            Long veterinarioId, Long autorId) {
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
}