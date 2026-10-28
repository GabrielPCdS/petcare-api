package com.petcareconnect.api.pet;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PetCriarRequest(
        @NotBlank(message = "Informe o nome do pet.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @NotBlank(message = "Informe o tipo do pet.")
        @Size(max = 50, message = "O tipo deve ter no máximo 50 caracteres.")
        String tipo,

        @Size(max = 100, message = "A raça deve ter no máximo 100 caracteres.")
        String raca,

        @Min(value = 0, message = "A idade não pode ser negativa.")
        @Max(value = 100, message = "Idade inválida.")
        Integer idade,

        @DecimalMin(value = "0.0", message = "O peso não pode ser negativo.")
        @Digits(integer = 4, fraction = 2, message = "Peso inválido (até 4 dígitos e 2 casas decimais).")
        BigDecimal peso,

        Long tutorId) {
}