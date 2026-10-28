package com.petcareconnect.api.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petcareconnect.api.common.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/Auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/cadastro")
    public ApiResponse<Long> cadastro(@Valid @RequestBody CadastroRequest req) {
        return ApiResponse.ok(authService.cadastrar(req));
    }

    @PostMapping("/recuperar-senha")
    public ApiResponse<RecuperarSenhaResponse> recuperarSenha(@Valid @RequestBody RecuperarSenhaRequest req) {
        return ApiResponse.ok(authService.recuperarSenha(req));
    }

    @PostMapping("/nova-senha")
    public ApiResponse<Void> novaSenha(@Valid @RequestBody NovaSenhaRequest req) {
        authService.novaSenha(req);
        return ApiResponse.okComMensagem("Senha alterada com sucesso.", null);
    }
}