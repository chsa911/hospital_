package com.example.hospital.service;

import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.Patient;
import com.example.hospital.entity.Stay;
import com.example.hospital.repository.HospitalRepository;
import com.example.hospital.repository.PatientRepository;
import com.example.hospital.repository.StayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service @RequiredArgsConstructor
public class StayService {

    private final StayRepository repo;
    private final PatientRepository patientRepo;
    private final HospitalRepository hospitalRepo;

    public Stay create(Long patientId, Long hospitalId, LocalDate start, LocalDate end) {
        Patient p = patientRepo.findById(patientId).orElseThrow();
        Hospital h = hospitalRepo.findById(hospitalId).orElseThrow();
        return repo.save(Stay.builder().patient(p).hospital(h).startDate(start).endDate(end).cancelled(false).build());
    }

    public Stay cancel(Long stayId) {
        Stay s = repo.findById(stayId).orElseThrow();
        s.setCancelled(true);
        return repo.save(s);
    }

    public List<Stay> listByPatient(Long patientId) {
        return repo.findByPatient_Id(patientId);
    }

    public List<Stay> overlapping(Long patientId, LocalDate start, LocalDate end) {
        return repo.findOverlapping(patientId, start, end);
    }
}
