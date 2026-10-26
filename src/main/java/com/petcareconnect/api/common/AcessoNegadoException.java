package com.petcareconnect.api.common;

import org.springframework.http.HttpStatus;

/** Usada nos serviços quando o usuário não pode ver/alterar aquele dado (ex.: pet de outro tutor). */
public class AcessoNegadoException extends NegocioException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem, HttpStatus.FORBIDDEN);
    }
}