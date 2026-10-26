package com.petcareconnect.api.relatorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RelatorioRepository extends JpaRepository<Relatorio, Long> {

    Optional<Relatorio> findByPetId(Long petId);

    List<Relatorio> findByPetTutorId(Long tutorId);
}