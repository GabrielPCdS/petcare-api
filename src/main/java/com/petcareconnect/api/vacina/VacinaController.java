package com.petcareconnect.api.vacina;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petcareconnect.api.common.ApiResponse;
import com.petcareconnect.api.security.UsuarioLogado;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/Vacinas")
public class VacinaController {

    private final VacinaService service;

    public VacinaController(VacinaService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<VacinaResponse>> listar() {
        return ApiResponse.ok(service.listar(UsuarioLogado.atual()));
    }

    @GetMapping("/{petId}")
    public ApiResponse<List<VacinaResponse>> listarDoPet(@PathVariable Long petId) {
        return ApiResponse.ok(service.listarDoPet(petId, UsuarioLogado.atual()));
    }

    @PostMapping
    public ApiResponse<Long> criar(@Valid @RequestBody VacinaCriarRequest req) {
        return ApiResponse.okComMensagem("Vacina registrada com sucesso.",
                service.criar(req, UsuarioLogado.atual()));
    }

    @PatchMapping("/{id}/aplicar")
    public ApiResponse<Void> aplicar(@PathVariable Long id,
                                     @RequestParam(required = false) String data,
                                     @RequestParam(required = false) String obs) {
        service.aplicar(id, data, obs, UsuarioLogado.atual());
        return ApiResponse.<Void>okComMensagem("Vacina marcada como aplicada.", null);
    }
}