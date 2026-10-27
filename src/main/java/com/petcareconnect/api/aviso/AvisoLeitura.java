package com.petcareconnect.api.aviso;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "aviso_leitura")
public class AvisoLeitura {

    @EmbeddedId
    private AvisoLeituraId id;

    public AvisoLeitura() {
    }

    public AvisoLeitura(Long avisoId, Long usuarioId) {
        this.id = new AvisoLeituraId(avisoId, usuarioId);
    }

    public AvisoLeituraId getId() { return id; }
    public void setId(AvisoLeituraId id) { this.id = id; }
}