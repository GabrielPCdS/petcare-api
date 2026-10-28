package com.petcareconnect.api.pet;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petcareconnect.api.common.ApiResponse;
import com.petcareconnect.api.security.UsuarioLogado;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/Pets")
public class PetController {

    private final PetService service;

    public PetController(PetService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<PetResponse>> listar() {
        return ApiResponse.ok(service.listar(UsuarioLogado.atual()));
    }

    @GetMapping("/{id}")
    public ApiResponse<PetResponse> obter(@PathVariable Long id) {
        return ApiResponse.ok(service.obter(id, UsuarioLogado.atual()));
    }

    @PostMapping
    public ApiResponse<Long> criar(@Valid @RequestBody PetCriarRequest req) {
        return ApiResponse.okComMensagem("Pet cadastrado com sucesso.",
                service.criar(req, UsuarioLogado.atual()));
    }

    @PatchMapping("/{id}")
    public ApiResponse<Void> atualizar(@PathVariable Long id,
                                       @Valid @RequestBody PetAtualizarRequest req) {
        service.atualizar(id, req, UsuarioLogado.atual());
        return ApiResponse.okComMensagem("Pet atualizado com sucesso.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> remover(@PathVariable Long id) {
        service.remover(id, UsuarioLogado.atual());
        return ApiResponse.okComMensagem("Pet removido com sucesso.", null);
    }
}