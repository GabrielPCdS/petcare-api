package com.petcareconnect.api.vacina;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VacinaCriarRequest(
        @NotNull(message = "Informe o pet.") Long petId,

        @NotBlank(message = "Informe o nome da vacina.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,

        // dd/MM/yyyy ou vazio
        String dataAplicacao,

        @Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres.")
        String observacao,

        Boolean aplicada) {
}