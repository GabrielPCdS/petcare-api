package com.petcareconnect.api.auth;

import jakarta.validation.constraints.NotBlank;

public record RecuperarSenhaRequest(
        @NotBlank(message = "Informe o login.") String login,
        @NotBlank(message = "Informe o e-mail.") String email) {
}