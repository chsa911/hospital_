package com.example.hospital.repository;

import com.example.hospital.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {

    // Use patient.id and hospital.id in the query
    List<Bill> findByPatientIdAndHospitalId(Long patientId, Long hospitalId);

    // Same with unpaid filter
    List<Bill> findByPatientIdAndHospitalIdAndPaidFalse(Long patientId, Long hospitalId);

    // Or if you want by just patient:
    List<Bill> findByPatientId(Long patientId);
}
