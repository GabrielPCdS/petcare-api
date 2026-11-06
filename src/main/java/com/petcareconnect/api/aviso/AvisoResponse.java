package com.petcareconnect.api.aviso;

public record AvisoResponse(
        Long id,
        String titulo,
        String texto,
        String tipo,
        Long tutorId,
        Long petId,
        boolean lido) {

    public static AvisoResponse de(Aviso a, boolean lido) {
        return new AvisoResponse(a.getId(), a.getTitulo(), a.getTexto(), a.getTipo(),
                a.getTutorId(), a.getPetId(), lido);
    }
}