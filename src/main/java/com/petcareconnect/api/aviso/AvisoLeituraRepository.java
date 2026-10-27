package com.petcareconnect.api.aviso;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvisoLeituraRepository extends JpaRepository<AvisoLeitura, AvisoLeituraId> {

    // Ids dos avisos que este usuario ja leu (para preencher o campo "lido")
    @Query("select l.id.avisoId from AvisoLeitura l where l.id.usuarioId = :usuarioId")
    Set<Long> findAvisoIdsLidosPor(@Param("usuarioId") Long usuarioId);
}