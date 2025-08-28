package com.example.hospital.repository;

import com.example.hospital.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByPatient_Id(Long patientId);
    List<Bill> findByPatient_IdAndHospital_IdAndPaidFalse(Long patientId, Long hospitalId);
}
