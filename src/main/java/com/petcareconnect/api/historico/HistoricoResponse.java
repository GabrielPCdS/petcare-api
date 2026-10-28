package com.petcareconnect.api.historico;

import com.petcareconnect.api.common.Formatos;

public record HistoricoResponse(Long id, Long petId, String descricao, String dataEvento) {

    public static HistoricoResponse de(Historico h) {
        return new HistoricoResponse(h.getId(), h.getPet().getId(), h.getDescricao(),
                h.getDataEvento().format(Formatos.DATA));
    }
}