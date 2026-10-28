package com.petcareconnect.api.relatorio;

import com.petcareconnect.api.common.Formatos;

public record RelatorioResponse(
        Long id,
        Long petId,
        String petNome,
        String tutorNome,
        String texto,
        String status,
        String atualizacao) {

    public static RelatorioResponse de(Relatorio r) {
        return new RelatorioResponse(r.getId(), r.getPet().getId(), r.getPet().getNome(),
                r.getPet().getTutor().getNome(), r.getTexto(), r.getStatus(),
                r.getAtualizadoEm() != null ? r.getAtualizadoEm().format(Formatos.DATA_HORA) : null);
    }
}