package com.example.hospital.repository;

import com.example.hospital.entity.Stay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface StayRepository extends JpaRepository<Stay, Long> {
    List<Stay> findByPatient_Id(Long patientId);

    @Query("""
       select s from Stay s
       where s.patient.id = :patientId
         and s.cancelled = false
         and s.startDate <= :end
         and s.endDate   >= :start
    """)
    List<Stay> findOverlapping(Long patientId, LocalDate start, LocalDate end);
}
