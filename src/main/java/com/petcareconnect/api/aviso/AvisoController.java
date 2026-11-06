package com.petcareconnect.api.aviso;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petcareconnect.api.common.ApiResponse;
import com.petcareconnect.api.security.UsuarioLogado;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/Avisos")
public class AvisoController {

    private final AvisoService service;

    public AvisoController(AvisoService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<AvisoResponse>> listar() {
        return ApiResponse.ok(service.listar(UsuarioLogado.atual()));
    }

    @PostMapping
    public ApiResponse<Long> criar(@Valid @RequestBody AvisoRequest req) {
        return ApiResponse.okComMensagem("Aviso enviado com sucesso.",
                service.criar(req, UsuarioLogado.atual()));
    }

    @PostMapping("/{id}/lido")
    public ApiResponse<Void> marcarLido(@PathVariable Long id) {
        service.marcarLido(id, UsuarioLogado.atual());
        return ApiResponse.<Void>okComMensagem("Aviso marcado como lido.", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> atualizar(@PathVariable Long id, @Valid @RequestBody AvisoRequest req) {
        service.atualizar(id, req, UsuarioLogado.atual());
        return ApiResponse.<Void>okComMensagem("Aviso atualizado com sucesso.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> apagar(@PathVariable Long id) {
        service.apagar(id);
        return ApiResponse.<Void>okComMensagem("Aviso removido com sucesso.", null);
    }
}