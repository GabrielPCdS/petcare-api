package com.petcareconnect.api.relatorio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RelatorioCriarRequest(
        @NotNull(message = "Informe o pet.") Long petId,

        @NotBlank(message = "Informe o texto do relatório.")
        @Size(max = 5000, message = "O texto deve ter no máximo 5000 caracteres.")
        String texto) {
}