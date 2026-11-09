package com.petcareconnect.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.petcareconnect.api.usuario.Usuario;

class VeterinarioListaTest extends IntegracaoBase {

    @Test
    void tutorVeApenasVeterinariosAtivosSemDadosSensiveis() throws Exception {
        Usuario tutor = criarUsuario("Tutor");
        Usuario ativo = criarUsuario("Veterinario");
        Usuario inativo = criarUsuario("Veterinario");
        inativo.setAtivo(false);
        usuarios.save(inativo);

        String corpo = mvc.perform(get("/api/Veterinarios").header("Authorization", bearer(tutor)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode dados = json.readTree(corpo).get("dados");
        Set<Long> ids = new HashSet<>();
        dados.forEach(n -> {
            ids.add(n.get("id").asLong());
            assertFalse(n.has("email"));
            assertFalse(n.has("login"));
        });

        assertTrue(ids.contains(ativo.getId()));
        assertFalse(ids.contains(inativo.getId()));
    }

    @Test
    void semTokenDa401() throws Exception {
        mvc.perform(get("/api/Veterinarios"))
                .andExpect(status().isUnauthorized());
    }
}