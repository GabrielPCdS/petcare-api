package com.petcareconnect.api.auth;

public record LoginResponse(String token, Long usuarioId, String nome, String login, String perfil) {
}