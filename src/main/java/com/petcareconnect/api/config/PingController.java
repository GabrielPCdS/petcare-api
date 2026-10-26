package com.petcareconnect.api.config;

import com.petcareconnect.api.common.ApiResponse;
import com.petcareconnect.api.common.NegocioException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** TEMPORÁRIO: só para validar a etapa 1. Será removido. */
@RestController
@RequestMapping("/api/ping")
public class PingController {

    public record TesteDto(@NotBlank(message = "O nome é obrigatório.") String nome) {}

    @GetMapping
    public ApiResponse<Map<String, String>> ping() {
        return ApiResponse.ok(Map.of("status", "PetCare Connect API no ar"));
    }

    @GetMapping("/erro")
    public ApiResponse<Void> erro() {
        throw new NegocioException("Exemplo de erro de negócio.");
    }

    @GetMapping("/falha")
    public ApiResponse<Void> falha() {
        throw new IllegalStateException("detalhe interno que NÃO pode vazar");
    }

    @PostMapping("/validar")
    public ApiResponse<Void> validar(@Valid @RequestBody TesteDto corpo) {
        return ApiResponse.ok();
    }
}