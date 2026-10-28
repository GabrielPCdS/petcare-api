package com.petcareconnect.api.consulta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConsultaCriarRequest(
        @NotNull(message = "Informe o pet.") Long petId,
        @NotNull(message = "Informe o veterinário.") Long veterinarioId,
        @NotBlank(message = "Informe a data.") String data,
        @NotBlank(message = "Informe a hora.") String hora,
        @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.")
        String observacoes) {
}