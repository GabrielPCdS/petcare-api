package com.petcareconnect.api.aviso;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    // Tutor: avisos gerais e os enderecados a ele
    @Query("select a from Aviso a where a.tipo = 'geral' or a.tutorId = :tutorId order by a.criadoEm desc")
    List<Aviso> findVisiveisParaTutor(@Param("tutorId") Long tutorId);

    // Veterinario: gerais, enderecados a ele (tipo vet) e os que ele proprio criou
    @Query("select a from Aviso a where a.tipo = 'geral' "
            + "or (a.tipo = 'vet' and a.veterinarioId = :vetId) "
            + "or a.autorId = :vetId order by a.criadoEm desc")
    List<Aviso> findVisiveisParaVeterinario(@Param("vetId") Long vetId);

    // Admin: todos
    List<Aviso> findAllByOrderByCriadoEmDesc();
}