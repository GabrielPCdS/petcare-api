package com.petcareconnect.api.pet;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PetRepository extends JpaRepository<Pet, Long> {

    @Override
    @EntityGraph(attributePaths = "tutor")
    List<Pet> findAll();

    @EntityGraph(attributePaths = "tutor")
    List<Pet> findByTutorId(Long tutorId);

    boolean existsByTutorIdAndNomeIgnoreCase(Long tutorId, String nome);

    boolean existsByTutorIdAndNomeIgnoreCaseAndIdNot(Long tutorId, String nome, Long id);

    @Query("select distinct p from Pet p join fetch p.tutor "
            + "where p.id in (select c.pet.id from Consulta c where c.veterinario.id = :vetId)")
    List<Pet> findAtendidosPeloVeterinario(@Param("vetId") Long vetId);

    @Query("select count(c) > 0 from Consulta c where c.pet.id = :petId and c.veterinario.id = :vetId")
    boolean veterinarioAtende(@Param("petId") Long petId, @Param("vetId") Long vetId);
}