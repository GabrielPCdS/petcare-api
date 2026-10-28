package com.petcareconnect.api.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NovaSenhaRequest(
        @NotBlank(message = "Informe o login.") String login,

        @NotBlank(message = "Informe a nova senha.")
        @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.")
        String novaSenha,

        @NotBlank(message = "Informe o token.") String token) {
}