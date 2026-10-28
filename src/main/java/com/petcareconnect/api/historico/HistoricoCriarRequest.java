package com.petcareconnect.api.historico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HistoricoCriarRequest(
        @NotNull(message = "Informe o pet.") Long petId,

        @NotBlank(message = "Informe a descrição.")
        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres.")
        String descricao,

        @NotBlank(message = "Informe a data do evento.") String dataEvento) {
}