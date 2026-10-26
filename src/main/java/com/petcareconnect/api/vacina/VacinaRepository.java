package com.petcareconnect.api.vacina;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VacinaRepository extends JpaRepository<Vacina, Long> {

    List<Vacina> findByPetId(Long petId);

    List<Vacina> findByPetTutorId(Long tutorId);
}