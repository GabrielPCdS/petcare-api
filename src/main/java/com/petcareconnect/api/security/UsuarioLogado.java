package com.petcareconnect.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public record UsuarioLogado(Long id, String nome, String login, String perfil) {

    public static UsuarioLogado atual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioLogado usuario) {
            return usuario;
        }
        throw new IllegalStateException("Nenhum usuario autenticado nesta requisicao.");
    }
}