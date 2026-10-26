package com.petcareconnect.api.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Envelope padrão de todas as respostas da API.
 * NON_NULL: campos nulos (mensagem, dados) não aparecem no JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean sucesso, String mensagem, T dados) {

    public static <T> ApiResponse<T> ok(T dados) {
        return new ApiResponse<>(true, null, dados);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static <T> ApiResponse<T> okComMensagem(String mensagem, T dados) {
        return new ApiResponse<>(true, mensagem, dados);
    }

    public static ApiResponse<Void> erro(String mensagem) {
        return new ApiResponse<>(false, mensagem, null);
    }
}