package com.petcareconnect.api;

import static org.springframework.security.config.http.MatcherType.mvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.petcareconnect.api.consulta.Consulta;
import com.petcareconnect.api.pet.Pet;
import com.petcareconnect.api.usuario.Usuario;

class RelatorioRn003Test extends IntegracaoBase {

    private static final String MSG_RN003 =
            "O pet deve ter ao menos uma consulta com status \"Realizada\" para gerar relatório (Regra RN-003).";
    private static final LocalDate DATA = AGORA.toLocalDate().plusDays(10);

    @Test
    void petSemNenhumaConsultaNaoGeraRelatorio() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Usuario admin = criarUsuario(Usuario.ADMIN);
        Pet pet = criarPet(tutor);

        salvarRelatorio(bearer(admin), pet, "Texto")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(MSG_RN003));
    }

    @Test
    void petSoComConsultaMarcadaNaoGeraRelatorio() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Usuario vet = criarUsuario(Usuario.VETERINARIO);
        Pet pet = criarPet(tutor);
        criarConsulta(pet, vet, DATA, LocalTime.of(9, 0), Consulta.MARCADA);

        salvarRelatorio(bearer(vet), pet, "Texto")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(MSG_RN003));
    }

    @Test
    void consultaCanceladaOuNaoCompareceuNaoConta() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Usuario vet = criarUsuario(Usuario.VETERINARIO);
        Usuario admin = criarUsuario(Usuario.ADMIN);
        Pet pet = criarPet(tutor);
        criarConsulta(pet, vet, DATA, LocalTime.of(9, 0), Consulta.CANCELADA);
        criarConsulta(pet, vet, DATA, LocalTime.of(10, 0), Consulta.NAO_COMPARECEU);

        salvarRelatorio(bearer(admin), pet, "Texto")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(MSG_RN003));
    }

    @Test
    void comConsultaRealizadaORelatorioEhCriadoEDepoisAtualizado() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Usuario vet = criarUsuario(Usuario.VETERINARIO);
        Pet pet = criarPet(tutor);
        criarConsulta(pet, vet, AGORA.toLocalDate().minusDays(3), LocalTime.of(9, 0), Consulta.REALIZADA);

        long primeiro = idDe(salvarRelatorio(bearer(vet), pet, "Primeira versao").andExpect(status().isOk()));
        long segundo = idDe(salvarRelatorio(bearer(vet), pet, "Segunda versao").andExpect(status().isOk()));

        // Um relatorio por pet: o segundo POST atualiza o mesmo registro
        org.junit.jupiter.api.Assertions.assertEquals(primeiro, segundo);

        mvc.perform(get("/api/Relatorios").param("petId", String.valueOf(pet.getId()))
                        .header("Authorization", bearer(tutor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.length()").value(1))
                .andExpect(jsonPath("$.dados[0].texto").value("Segunda versao"))
                .andExpect(jsonPath("$.dados[0].status").value("Disponivel"));
    }

    @Test
    void tutorNaoPodeCriarRelatorio() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Pet pet = criarPet(tutor);

        salvarRelatorio(bearer(tutor), pet, "Texto").andExpect(status().isForbidden());
    }
}