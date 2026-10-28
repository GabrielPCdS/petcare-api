package com.petcareconnect.api.usuario;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petcareconnect.api.common.ApiResponse;
import com.petcareconnect.api.security.UsuarioLogado;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/Usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<UsuarioResponse>> listar(@RequestParam(required = false) String perfil) {
        return ApiResponse.ok(service.listar(perfil));
    }

    @PostMapping
    public ApiResponse<UsuarioResponse> criar(@Valid @RequestBody UsuarioCriarRequest req) {
        return ApiResponse.okComMensagem("Usuário criado com sucesso.", service.criar(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<UsuarioResponse> atualizar(@PathVariable Long id,
                                                  @Valid @RequestBody UsuarioAtualizarRequest req) {
        return ApiResponse.okComMensagem("Usuário atualizado com sucesso.", service.atualizar(id, req));
    }

    // Corpo e um boolean puro: true ou false
    @PatchMapping("/{id}/ativo")
    public ApiResponse<Void> alterarAtivo(@PathVariable Long id, @RequestBody Boolean ativo) {
        service.alterarAtivo(id, ativo, UsuarioLogado.atual());
        return ApiResponse.okComMensagem(Boolean.TRUE.equals(ativo) ? "Acesso ativado." : "Acesso desativado.", null);
    }
}