package com.petcareconnect.api.aviso;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AvisoLeituraId implements Serializable {

    @Column(name = "aviso_id")
    private Long avisoId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    public AvisoLeituraId() {
    }

    public AvisoLeituraId(Long avisoId, Long usuarioId) {
        this.avisoId = avisoId;
        this.usuarioId = usuarioId;
    }

    public Long getAvisoId() { return avisoId; }
    public void setAvisoId(Long avisoId) { this.avisoId = avisoId; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AvisoLeituraId outro)) return false;
        return Objects.equals(avisoId, outro.avisoId)
                && Objects.equals(usuarioId, outro.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(avisoId, usuarioId);
    }
}