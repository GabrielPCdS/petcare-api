package com.petcareconnect.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.petcareconnect.api.consulta.Consulta;
import com.petcareconnect.api.pet.Pet;
import com.petcareconnect.api.usuario.Usuario;

// "Agora" nos testes e 10/06/2030 as 10:00
class ConsultaRn005Test extends IntegracaoBase {

    private static final LocalDate HOJE = AGORA.toLocalDate();

    private String statusNoBanco(long id) {
        return consultas.findById(id).orElseThrow().getStatus();
    }

    @Test
    void cancelarComMenosDe24hViraNaoCompareceu() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Usuario vet = criarUsuario(Usuario.VETERINARIO);
        Pet pet = criarPet(tutor);

        // 11/06 09:00 = 23h depois do "agora"
        long id = idDe(agendar(bearer(tutor), pet, vet, HOJE.plusDays(1), "09:00").andExpect(status().isOk()));

        cancelar(bearer(tutor), id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("NaoCompareceu")));

        assertEquals(Consulta.NAO_COMPARECEU, statusNoBanco(id));
    }

    @Test
    void cancelarComExatamente24hViraCancelada() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Usuario vet = criarUsuario(Usuario.VETERINARIO);
        Pet pet = criarPet(tutor);

        // 11/06 10:00 = exatamente 24h depois do "agora"
        long id = idDe(agendar(bearer(tutor), pet, vet, HOJE.plusDays(1), "10:00").andExpect(status().isOk()));

        cancelar(bearer(tutor), id).andExpect(status().isOk());

        assertEquals(Consulta.CANCELADA, statusNoBanco(id));
    }

    @Test
    void cancelarComMaisDe24hViraCancelada() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Usuario vet = criarUsuario(Usuario.VETERINARIO);
        Pet pet = criarPet(tutor);

        long id = idDe(agendar(bearer(tutor), pet, vet, HOJE.plusDays(5), "14:00").andExpect(status().isOk()));

        cancelar(bearer(tutor), id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Consulta cancelada com sucesso."));

        assertEquals(Consulta.CANCELADA, statusNoBanco(id));
    }

    @Test
    void soEPossivelCancelarConsultaMarcada() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        Usuario vet = criarUsuario(Usuario.VETERINARIO);
        Pet pet = criarPet(tutor);

        long id = idDe(agendar(bearer(tutor), pet, vet, HOJE.plusDays(5), "15:00").andExpect(status().isOk()));
        cancelar(bearer(tutor), id).andExpect(status().isOk());

        cancelar(bearer(tutor), id)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Só é possível cancelar consultas com status Marcada."));
    }

    @Test
    void tutorNaoCancelaConsultaDeOutroTutor() throws Exception {
        Usuario dono = criarUsuario(Usuario.TUTOR);
        Usuario intruso = criarUsuario(Usuario.TUTOR);
        Usuario vet = criarUsuario(Usuario.VETERINARIO);
        Pet pet = criarPet(dono);

        long id = idDe(agendar(bearer(dono), pet, vet, HOJE.plusDays(5), "16:00").andExpect(status().isOk()));

        cancelar(bearer(intruso), id).andExpect(status().isNotFound());

        assertEquals(Consulta.MARCADA, statusNoBanco(id));
    }
}