package com.petcareconnect.api.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioCriarRequest(
        @NotBlank(message = "Informe o nome.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,

        @NotBlank(message = "Informe o login.")
        @Size(min = 3, max = 60, message = "O login deve ter entre 3 e 60 caracteres.")
        String login,

        @NotBlank(message = "Informe a senha.")
        @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.")
        String senha,

        @NotBlank(message = "Informe o perfil.")
        @Pattern(regexp = "Tutor|Veterinario", message = "O perfil deve ser Tutor ou Veterinario.")
        String perfil,

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email,

        @NotBlank(message = "Informe o telefone.")
        @Size(max = 30, message = "O telefone deve ter no máximo 30 caracteres.")
        String telefone,

        @Size(max = 255, message = "O endereço deve ter no máximo 255 caracteres.")
        String endereco,

        @Size(max = 50, message = "O status do convênio deve ter no máximo 50 caracteres.")
        String convenioStatus,

        @Size(max = 50, message = "O registro deve ter no máximo 50 caracteres.")
        String registro) {
}