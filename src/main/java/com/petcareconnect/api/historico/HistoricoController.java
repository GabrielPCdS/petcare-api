package com.petcareconnect.api.historico;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petcareconnect.api.common.ApiResponse;
import com.petcareconnect.api.security.UsuarioLogado;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/Historico")
public class HistoricoController {

    private final HistoricoService service;

    public HistoricoController(HistoricoService service) {
        this.service = service;
    }

    @GetMapping("/{petId}")
    public ApiResponse<List<HistoricoResponse>> listar(@PathVariable Long petId) {
        return ApiResponse.ok(service.listar(petId, UsuarioLogado.atual()));
    }

    @PostMapping
    public ApiResponse<Long> criar(@Valid @RequestBody HistoricoCriarRequest req) {
        return ApiResponse.okComMensagem("Registro adicionado ao histórico.",
                service.criar(req, UsuarioLogado.atual()));
    }
}