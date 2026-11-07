package com.petcareconnect.api;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcareconnect.api.common.Formatos;
import com.petcareconnect.api.consulta.Consulta;
import com.petcareconnect.api.consulta.ConsultaRepository;
import com.petcareconnect.api.pet.Pet;
import com.petcareconnect.api.pet.PetRepository;
import com.petcareconnect.api.security.JwtService;
import com.petcareconnect.api.usuario.Usuario;
import org.testcontainers.containers.PostgreSQLContainer;
import com.petcareconnect.api.usuario.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(IntegracaoBase.RelogioDeTeste.class)
public abstract class IntegracaoBase {

    protected static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    // "Agora" fixo de todos os testes: 10/06/2030 as 10:00
    protected static final LocalDateTime AGORA = LocalDateTime.of(2030, 6, 10, 10, 0);
    protected static final String SENHA = "Senha123!";

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");
    private static final AtomicInteger SEQ = new AtomicInteger();

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void propriedadesDoBanco(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @TestConfiguration
    static class RelogioDeTeste {
        @Bean
        @Primary
        Clock relogioFixo() {
            return Clock.fixed(AGORA.atZone(ZONA).toInstant(), ZONA);
        }
    }

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;
    @Autowired protected UsuarioRepository usuarios;
    @Autowired protected PetRepository pets;
    @Autowired protected ConsultaRepository consultas;
    @Autowired protected PasswordEncoder encoder;
    @Autowired protected JwtService jwt;

    // ---------- dados de teste ----------

    protected Usuario criarUsuario(String perfil) {
        int n = SEQ.incrementAndGet();
        Usuario u = new Usuario();
        u.setNome("Usuario " + n);
        u.setLogin(perfil.toLowerCase() + "_" + UUID.randomUUID().toString().substring(0, 8));
        u.setEmail("u" + n + "@teste.com");
        u.setSenhaHash(encoder.encode(SENHA));
        u.setPerfil(perfil);
        u.setTelefone("11999999999");
        u.setAtivo(true);
        return usuarios.save(u);
    }

    protected Pet criarPet(Usuario tutor) {
        Pet p = new Pet();
        p.setNome("Pet " + SEQ.incrementAndGet());
        p.setTipo("Cachorro");
        p.setTutor(tutor);
        return pets.save(p);
    }

    protected Consulta criarConsulta(Pet pet, Usuario vet, LocalDate data, LocalTime hora, String status) {
        Consulta c = new Consulta();
        c.setPet(pet);
        c.setVeterinario(vet);
        c.setData(data);
        c.setHora(hora);
        c.setStatus(status);
        return consultas.saveAndFlush(c);
    }

    protected String bearer(Usuario u) {
        return "Bearer " + jwt.gerar(u);
    }

    // ---------- chamadas HTTP ----------

    protected ResultActions agendar(String bearer, Pet pet, Usuario vet, LocalDate data, String hora)
            throws Exception {
        String corpo = "{\"petId\":" + pet.getId() + ",\"veterinarioId\":" + vet.getId()
                + ",\"data\":\"" + data.format(Formatos.DATA) + "\",\"hora\":\"" + hora + "\"}";
        return mvc.perform(MockMvcRequestBuilders.post("/api/Consultas")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo));
    }

    protected ResultActions cancelar(String bearer, long consultaId) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/Consultas/" + consultaId + "/cancelar")
                .header("Authorization", bearer));
    }

    protected ResultActions salvarRelatorio(String bearer, Pet pet, String texto) throws Exception {
        String corpo = "{\"petId\":" + pet.getId() + ",\"texto\":\"" + texto + "\"}";
        return mvc.perform(MockMvcRequestBuilders.post("/api/Relatorios")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo));
    }

    // Le o id numerico que a API devolve em "dados"
    protected long idDe(ResultActions resposta) throws Exception {
        String corpo = resposta.andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return json.readTree(corpo).get("dados").asLong();
    }
}