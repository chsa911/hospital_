package com.example.hospital.service;

import com.example.hospital.entity.Patient;
import com.example.hospital.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class PatientService {
    private final PatientRepository repo;

    public Patient create(String first, String last) {
        return repo.save(Patient.builder().firstName(first).lastName(last).build());
    }
    public Patient update(Long id, String first, String last) {
        Patient p = repo.findById(id).orElseThrow();
        p.setFirstName(first); p.setLastName(last);
        return repo.save(p);
    }
    public void delete(Long id) { repo.deleteById(id); }
    public Patient get(Long id) { return repo.findById(id).orElseThrow(); }
}
