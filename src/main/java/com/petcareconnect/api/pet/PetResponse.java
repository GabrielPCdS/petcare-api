package com.petcareconnect.api.pet;

import java.math.BigDecimal;

import com.petcareconnect.api.common.Formatos;

public record PetResponse(
        Long id,
        String nome,
        String tipo,
        String raca,
        Integer idade,
        BigDecimal peso,
        Long tutorId,
        String tutorNome,
        String criadoEm) {

    public static PetResponse de(Pet p) {
        return new PetResponse(p.getId(), p.getNome(), p.getTipo(), p.getRaca(), p.getIdade(),
                p.getPeso(), p.getTutor().getId(), p.getTutor().getNome(),
                p.getCriadoEm() != null ? p.getCriadoEm().format(Formatos.DATA_HORA) : null);
    }
}