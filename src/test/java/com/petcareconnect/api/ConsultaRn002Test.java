package com.petcareconnect.api;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.petcareconnect.api.common.Formatos;
import com.petcareconnect.api.consulta.Consulta;
import com.petcareconnect.api.pet.Pet;
import com.petcareconnect.api.usuario.Usuario;

class ConsultaRn002Test extends IntegracaoBase {

    private static final String MSG_RN002 = "Horário já reservado para este veterinário (Regra RN-002).";
    private static final LocalDate DATA = AGORA.toLocalDate().plusDays(10);

    @Test
    void mesmoHorarioParaOMesmoVeterinarioEhRecusado() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Usuario vet = criarUsuario(Usuario.VETERINARIO);
        Pet pet1 = criarPet(tutor);
        Pet pet2 = criarPet(tutor);

        agendar(bearer(tutor), pet1, vet, DATA, "09:00").andExpect(status().isOk());

        agendar(bearer(tutor), pet2, vet, DATA, "09:00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(MSG_RN002));
    }

    @Test
    void mesmoHorarioComOutroVeterinarioEhPermitido() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Usuario vet1 = criarUsuario(Usuario.VETERINARIO);
        Usuario vet2 = criarUsuario(Usuario.VETERINARIO);
        Pet pet1 = criarPet(tutor);
        Pet pet2 = criarPet(tutor);

        agendar(bearer(tutor), pet1, vet1, DATA, "09:00").andExpect(status().isOk());
        agendar(bearer(tutor), pet2, vet2, DATA, "09:00").andExpect(status().isOk());
    }

    @Test
    void consultaCanceladaLiberaOHorario() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Usuario vet = criarUsuario(Usuario.VETERINARIO);
        Pet pet = criarPet(tutor);

        long id = idDe(agendar(bearer(tutor), pet, vet, DATA, "10:00").andExpect(status().isOk()));
        cancelar(bearer(tutor), id).andExpect(status().isOk());

        agendar(bearer(tutor), pet, vet, DATA, "10:00").andExpect(status().isOk());
    }

    @Test
    void naoCompareceuContinuaOcupandoOHorario() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Usuario vet = criarUsuario(Usuario.VETERINARIO);
        Pet pet = criarPet(tutor);
        LocalDate amanha = AGORA.toLocalDate().plusDays(1);

        // Menos de 24h de antecedencia: o cancelamento vira NaoCompareceu
        long id = idDe(agendar(bearer(tutor), pet, vet, amanha, "09:00").andExpect(status().isOk()));
        cancelar(bearer(tutor), id).andExpect(status().isOk());

        agendar(bearer(tutor), pet, vet, amanha, "09:00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(MSG_RN002));
    }

    @Test
    void bancoImpedeDuasConsultasAtivasNoMesmoHorario() {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Usuario vet = criarUsuario(Usuario.VETERINARIO);
        Pet pet1 = criarPet(tutor);
        Pet pet2 = criarPet(tutor);
        LocalTime hora = LocalTime.of(11, 0);

        criarConsulta(pet1, vet, DATA, hora, Consulta.MARCADA);

        // Mesmo sem passar pelo servico (corrida entre requisicoes), o indice unico segura
        assertThrows(DataIntegrityViolationException.class,
                () -> criarConsulta(pet2, vet, DATA, hora, Consulta.MARCADA));

        // Cancelada nao conta
        criarConsulta(pet2, vet, DATA, hora, Consulta.CANCELADA);
    }

    @Test
    void horariosDisponiveisDescontamConsultasNaoCanceladas() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Usuario vet = criarUsuario(Usuario.VETERINARIO);
        Pet pet = criarPet(tutor);

        criarConsulta(pet, vet, DATA, LocalTime.of(9, 0), Consulta.MARCADA);
        criarConsulta(pet, vet, DATA, LocalTime.of(9, 30), Consulta.CANCELADA);

        mvc.perform(get("/api/Consultas/horarios-disponiveis")
                        .param("veterinarioId", String.valueOf(vet.getId()))
                        .param("data", DATA.format(Formatos.DATA))
                        .header("Authorization", bearer(tutor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados", not(hasItem("09:00"))))
                .andExpect(jsonPath("$.dados", hasItem("09:30")))
                .andExpect(jsonPath("$.dados", hasItem("08:00")))
                .andExpect(jsonPath("$.dados", hasItem("17:30")));
    }
}