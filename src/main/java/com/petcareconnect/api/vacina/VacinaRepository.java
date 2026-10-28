package com.petcareconnect.api.vacina;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VacinaRepository extends JpaRepository<Vacina, Long> {

    @Override
    @EntityGraph(attributePaths = "pet")
    List<Vacina> findAll();

    @EntityGraph(attributePaths = "pet")
    List<Vacina> findByPetId(Long petId);

    @EntityGraph(attributePaths = "pet")
    List<Vacina> findByPetTutorId(Long tutorId);

    // Veterinario: vacinas dos pets que aparecem em consultas dele
    @Query("select v from Vacina v join fetch v.pet "
            + "where v.pet.id in (select c.pet.id from Consulta c where c.veterinario.id = :vetId)")
    List<Vacina> findDosPetsAtendidosPor(@Param("vetId") Long vetId);
}