package com.petcareconnect.api.pet;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByTutorId(Long tutorId);
}