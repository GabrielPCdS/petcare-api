package com.petcareconnect.api.aviso;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvisoService {

    private final AvisoRepository avisos;

    public AvisoService(AvisoRepository avisos) {
        this.avisos = avisos;
    }

    @Transactional
    public void criarAutomatico(String titulo, String texto, String tipo, Long tutorId,
                                Long petId, Long veterinarioId, Long autorId) {
        Aviso a = new Aviso();
        a.setTitulo(titulo);
        a.setTexto(texto);
        a.setTipo(tipo);
        a.setTutorId(tutorId);
        a.setPetId(petId);
        a.setVeterinarioId(veterinarioId);
        a.setAutorId(autorId);
        avisos.save(a);
    }
}