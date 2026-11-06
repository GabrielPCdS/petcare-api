package com.petcareconnect.api.aviso;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvisoLeituraRepository extends JpaRepository<AvisoLeitura, AvisoLeituraId> {

    @Query("select l.id.avisoId from AvisoLeitura l where l.id.usuarioId = :usuarioId")
    Set<Long> findAvisoIdsLidosPor(@Param("usuarioId") Long usuarioId);

    // Idempotente: marcar duas vezes (ou em requisicoes simultaneas) nao gera erro
    @Modifying
    @Query(value = "insert into aviso_leitura (aviso_id, usuario_id) values (:avisoId, :usuarioId) "
            + "on conflict do nothing", nativeQuery = true)
    void marcarLido(@Param("avisoId") Long avisoId, @Param("usuarioId") Long usuarioId);
}