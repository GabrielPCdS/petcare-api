package com.petcareconnect.api.usuario;

import java.util.Comparator;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petcareconnect.api.common.ApiResponse;

@RestController
@RequestMapping("/api/Veterinarios")
public class VeterinarioController {

    private final UsuarioRepository usuarios;

    public VeterinarioController(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    public ApiResponse<List<VeterinarioResponse>> listar() {
        List<VeterinarioResponse> lista = usuarios.findByPerfil("Veterinario").stream()
                .filter(Usuario::isAtivo)
                .sorted(Comparator.comparing(Usuario::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(VeterinarioResponse::de)
                .toList();
        return ApiResponse.ok(lista);
    }
}