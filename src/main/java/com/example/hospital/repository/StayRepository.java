package com.example.hospital.repository;

import com.example.hospital.entity.Stay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StayRepository extends JpaRepository<Stay, Long> {
    List<Stay> findByPatientId(Long patientId);

    @Query("""
       select s from Stay s
       where s.patient.id = :patientId
         and s.cancelled = false
         and s.startDate <= :end
         and s.endDate   >= :start
    """)
    List<Stay> findOverlapping(@Param("patientId") Long patientId,
                               @Param("start") LocalDate start,
                               @Param("end") LocalDate end);
}
