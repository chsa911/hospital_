package com.example.hospital.service;

import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.Patient;
import com.example.hospital.repository.HospitalRepository;
import com.example.hospital.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service @RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepo;
    private final PatientRepository patientRepo;

    public Hospital create(String name, String address) {
        return hospitalRepo.save(Hospital.builder().name(name).address(address).build());
    }

    public Hospital update(Long id, String name, String address) {
        Hospital h = hospitalRepo.findById(id).orElseThrow();
        h.setName(name);
        h.setAddress(address);
        return hospitalRepo.save(h);
    }

    public void delete(Long id) { hospitalRepo.deleteById(id); }

    public List<Hospital> list() { return hospitalRepo.findAll(); }

    public void registerPatient(Long patientId, Long hospitalId) {
        Patient p = patientRepo.findById(patientId).orElseThrow();
        Hospital h = hospitalRepo.findById(hospitalId).orElseThrow();
        p.getHospitals().add(h);
        h.getPatients().add(p);
        patientRepo.save(p);
        hospitalRepo.save(h);
    }

    public List<Patient> patientsOfHospital(Long hospitalId) {
        Hospital h = hospitalRepo.findById(hospitalId).orElseThrow();
        return new ArrayList<>(h.getPatients());
    }

    public List<Hospital> hospitalsOfPatient(Long patientId) {
        Patient p = patientRepo.findById(patientId).orElseThrow();
        return new ArrayList<>(p.getHospitals());
    }
}
