package com.petcareconnect.api.relatorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RelatorioRepository extends JpaRepository<Relatorio, Long> {

    @Override
    @EntityGraph(attributePaths = {"pet", "pet.tutor"})
    List<Relatorio> findAll();

    @EntityGraph(attributePaths = {"pet", "pet.tutor"})
    Optional<Relatorio> findByPetId(Long petId);

    @EntityGraph(attributePaths = {"pet", "pet.tutor"})
    List<Relatorio> findByPetTutorId(Long tutorId);

    // Veterinario: relatorios dos pets que aparecem em consultas dele
    @Query("select r from Relatorio r join fetch r.pet p join fetch p.tutor "
            + "where p.id in (select c.pet.id from Consulta c where c.veterinario.id = :vetId)")
    List<Relatorio> findDosPetsAtendidosPor(@Param("vetId") Long vetId);
}