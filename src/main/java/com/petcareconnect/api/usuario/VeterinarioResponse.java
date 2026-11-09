package com.petcareconnect.api.usuario;

public record VeterinarioResponse(Long id, String nome) {

    public static VeterinarioResponse de(Usuario u) {
        return new VeterinarioResponse(u.getId(), u.getNome());
    }
}