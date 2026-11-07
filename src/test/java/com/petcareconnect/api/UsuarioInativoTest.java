package com.petcareconnect.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.petcareconnect.api.usuario.Usuario;

class UsuarioInativoTest extends IntegracaoBase {

    @Test
    void tokenDeUsuarioDesativadoPassaADar401() throws Exception {
        Usuario tutor = criarUsuario("Tutor");
        String bearer = bearer(tutor);

        mvc.perform(get("/api/Pets").header("Authorization", bearer))
                .andExpect(status().isOk());

        tutor.setAtivo(false);
        usuarios.save(tutor);

        mvc.perform(get("/api/Pets").header("Authorization", bearer))
                .andExpect(status().isUnauthorized());
    }
}