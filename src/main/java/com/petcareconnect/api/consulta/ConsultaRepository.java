package com.petcareconnect.api.consulta;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByPetTutorId(Long tutorId);

    List<Consulta> findByVeterinarioId(Long veterinarioId);

    // RN-002: horario ja ocupado (qualquer status diferente do informado)
    boolean existsByVeterinarioIdAndDataAndHoraAndStatusNot(
            Long veterinarioId, LocalDate data, LocalTime hora, String status);

    // Horarios disponiveis: consultas do veterinario na data, exceto canceladas
    List<Consulta> findByVeterinarioIdAndDataAndStatusNot(
            Long veterinarioId, LocalDate data, String status);

    // RN-003: pet com ao menos uma consulta com o status informado
    boolean existsByPetIdAndStatus(Long petId, String status);
}