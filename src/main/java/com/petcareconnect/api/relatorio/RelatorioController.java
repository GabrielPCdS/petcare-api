package com.petcareconnect.api.relatorio;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petcareconnect.api.common.ApiResponse;
import com.petcareconnect.api.security.UsuarioLogado;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/Relatorios")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<RelatorioResponse>> listar(@RequestParam(required = false) Long petId) {
        return ApiResponse.ok(service.listar(petId, UsuarioLogado.atual()));
    }

    @PostMapping
    public ApiResponse<Long> salvar(@Valid @RequestBody RelatorioCriarRequest req) {
        return ApiResponse.okComMensagem("Relatório salvo com sucesso.",
                service.salvar(req, UsuarioLogado.atual()));
    }
}