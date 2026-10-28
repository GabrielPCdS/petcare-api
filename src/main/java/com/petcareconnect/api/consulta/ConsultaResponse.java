package com.petcareconnect.api.consulta;

import com.petcareconnect.api.common.Formatos;

public record ConsultaResponse(
        Long id,
        Long petId,
        String petNome,
        Long veterinarioId,
        String vetNome,
        String data,
        String hora,
        String status,
        String observacoes) {

    public static ConsultaResponse de(Consulta c) {
        return new ConsultaResponse(c.getId(), c.getPet().getId(), c.getPet().getNome(),
                c.getVeterinario().getId(), c.getVeterinario().getNome(),
                c.getData().format(Formatos.DATA), c.getHora().format(Formatos.HORA),
                c.getStatus(), c.getObservacoes());
    }
}