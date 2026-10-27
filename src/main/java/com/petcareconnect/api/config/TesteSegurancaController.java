package com.petcareconnect.api.config;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petcareconnect.api.common.ApiResponse;
import com.petcareconnect.api.security.UsuarioLogado;

// TEMPORARIO: remover junto com o PingController
@RestController
@RequestMapping("/api/teste")
public class TesteSegurancaController {

    @GetMapping("/autenticado")
    public ApiResponse<Map<String, Object>> autenticado() {
        UsuarioLogado u = UsuarioLogado.atual();
        return ApiResponse.ok(Map.of("id", u.id(), "login", u.login(), "perfil", u.perfil()));
    }

    @GetMapping("/admin")
    public ApiResponse<String> admin() {
        return ApiResponse.ok("Voce e Admin");
    }
}