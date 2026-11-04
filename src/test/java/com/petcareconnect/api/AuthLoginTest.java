package com.petcareconnect.api;

import static org.springframework.security.config.http.MatcherType.mvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import com.petcareconnect.api.usuario.Usuario;

class AuthLoginTest extends IntegracaoBase {

    private static final String MSG_INVALIDO = "Login ou senha inválidos.";

    private ResultActions login(String login, String senha, String perfil) throws Exception {
        String corpo = "{\"login\":\"" + login + "\",\"senha\":\"" + senha + "\""
                + (perfil != null ? ",\"perfil\":\"" + perfil + "\"" : "") + "}";
        return mvc.perform(post("/api/Auth/login").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    @Test
    void loginValidoDevolveTokenEPerfilReal() throws Exception {
        Usuario vet = criarUsuario(Usuario.VETERINARIO);

        login(vet.getLogin(), SENHA, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.token").isNotEmpty())
                .andExpect(jsonPath("$.dados.usuarioId").value(vet.getId()))
                .andExpect(jsonPath("$.dados.perfil").value("Veterinario"))
                .andExpect(jsonPath("$.dados.senha").doesNotExist())
                .andExpect(jsonPath("$.dados.senhaHash").doesNotExist());
    }

    @Test
    void perfilEnviadoNoCorpoEhIgnoradoEODevolvidoEhOReal() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);

        login(tutor.getLogin(), SENHA, "Admin")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.perfil").value("Tutor"));
    }

    @Test
    void senhaErradaRecebeMensagemGenerica() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);

        login(tutor.getLogin(), "senha-errada", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value(MSG_INVALIDO));
    }

    @Test
    void loginInexistenteRecebeAMesmaMensagemGenerica() throws Exception {
        login("nao_existe_" + System.nanoTime(), SENHA, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(MSG_INVALIDO));
    }

    @Test
    void usuarioInativoNaoConsegueEntrar() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);
        tutor.setAtivo(false);
        usuarios.save(tutor);

        login(tutor.getLogin(), SENHA, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(MSG_INVALIDO));
    }

    @Test
    void rotaProtegidaSemTokenResponde401NoEnvelope() throws Exception {
        mvc.perform(get("/api/Pets"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void perfilSemPermissaoResponde403NoEnvelope() throws Exception {
        Usuario tutor = criarUsuario(Usuario.TUTOR);

        mvc.perform(get("/api/Usuarios").header("Authorization", bearer(tutor)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.sucesso").value(false));
    }
}