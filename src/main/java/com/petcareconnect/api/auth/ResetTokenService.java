package com.petcareconnect.api.auth;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class ResetTokenService {

    private static final Duration VALIDADE = Duration.ofMinutes(15);

    private record Entrada(String login, Instant expiraEm) {
    }

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Entrada> tokens = new ConcurrentHashMap<>();

    public String gerar(String login) {
        limparExpirados();
        // Um token ativo por login: o novo invalida o anterior
        tokens.values().removeIf(e -> e.login().equalsIgnoreCase(login));

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        tokens.put(token, new Entrada(login, Instant.now().plus(VALIDADE)));
        return token;
    }

    // Valida e consome o token (uso unico). Retorna true se valido para este login.
    public boolean consumir(String token, String login) {
        if (token == null || login == null) {
            return false;
        }
        Entrada entrada = tokens.remove(token);
        if (entrada == null) {
            return false;
        }
        return Instant.now().isBefore(entrada.expiraEm())
                && entrada.login().equalsIgnoreCase(login);
    }

    private void limparExpirados() {
        Instant agora = Instant.now();
        tokens.values().removeIf(e -> agora.isAfter(e.expiraEm()));
    }
}