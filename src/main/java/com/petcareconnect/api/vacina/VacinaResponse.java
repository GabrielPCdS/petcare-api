package com.petcareconnect.api.vacina;

import com.petcareconnect.api.common.Formatos;

public record VacinaResponse(
        Long id,
        Long petId,
        String petNome,
        String nome,
        String dataAplicacao,
        String observacao,
        boolean aplicada) {

    public static VacinaResponse de(Vacina v) {
        return new VacinaResponse(v.getId(), v.getPet().getId(), v.getPet().getNome(), v.getNome(),
                v.getDataAplicacao() != null ? v.getDataAplicacao().format(Formatos.DATA) : null,
                v.getObservacao(), v.isAplicada());
    }
}