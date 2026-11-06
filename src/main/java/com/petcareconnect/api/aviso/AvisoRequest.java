package com.petcareconnect.api.aviso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AvisoRequest(
        @NotBlank(message = "Informe o título.")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
        String titulo,

        @NotBlank(message = "Informe o texto.")
        @Size(max = 2000, message = "O texto deve ter no máximo 2000 caracteres.")
        String texto,

        @NotBlank(message = "Informe o tipo.")
        String tipo,

        Long tutorId,
        Long petId,
        Long veterinarioId) {
}