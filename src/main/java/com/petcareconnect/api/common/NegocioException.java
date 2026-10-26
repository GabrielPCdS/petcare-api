package com.petcareconnect.api.common;

import org.springframework.http.HttpStatus;

/**
 * Erro de regra de negócio. A mensagem é escrita por nós, então é segura
 * para mostrar ao cliente. Por padrão responde 400.
 */
public class NegocioException extends RuntimeException {

    private final HttpStatus status;

    public NegocioException(String mensagem) {
        this(mensagem, HttpStatus.BAD_REQUEST);
    }

    public NegocioException(String mensagem, HttpStatus status) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}