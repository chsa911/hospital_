package com.example.hospital.service;

import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.Patient;
import com.example.hospital.repository.HospitalRepository;
import com.example.hospital.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for Hospitals and their association to Patients.
 * - Uses explicit NOT_FOUND errors (no bare Optional.get()).
 * - Wraps reads/writes in transactions so LAZY collections can be accessed safely.
 * - Prevents duplicate registrations.
 */
@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepo;
    private final PatientRepository patientRepo;

    // --- CRUD ---

    @Transactional
    public Hospital create(String name, String address) {
        return hospitalRepo.save(Hospital.builder()
                .name(name)
                .address(address)
                .build());
    }

    @Transactional
    public Hospital update(Long id, String name, String address) {
        Hospital h = hospitalRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hospital %d not found".formatted(id)));
        h.setName(name);
        h.setAddress(address);
        return hospitalRepo.save(h);
    }

    @Transactional
    public void delete(Long id) {
        if (!hospitalRepo.existsById(id)) {
            throw new EntityNotFoundException("Hospital %d not found".formatted(id));
        }
        hospitalRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Hospital> list() {
        return hospitalRepo.findAll();
    }

    // --- Association management ---

    /**
     * Registers a patient to a hospital.
     * Idempotent: if already linked, it returns without error.
     */
    @Transactional
    public void registerPatient(Long patientId, Long hospitalId) {
        Patient p = patientRepo.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient %d not found".formatted(patientId)));
        Hospital h = hospitalRepo.findById(hospitalId)
                .orElseThrow(() -> new EntityNotFoundException("Hospital %d not found".formatted(hospitalId)));

        // Prevent duplicate link (compare by id to avoid equals/hashCode pitfalls)
        boolean alreadyLinked = h.getPatients().stream()
                .anyMatch(existing -> existing.getId().equals(patientId));
        if (alreadyLinked) {
            return; // idempotent
        }

        // Link both sides
        p.getHospitals().add(h);
        h.getPatients().add(p);

        // Persist (either side is enough; saving both is harmless)
        patientRepo.save(p);
        hospitalRepo.save(h);
    }

    @Transactional(readOnly = true)
    public List<Patient> patientsOfHospital(Long hospitalId) {
        Hospital h = hospitalRepo.findById(hospitalId)
                .orElseThrow(() -> new EntityNotFoundException("Hospital %d not found".formatted(hospitalId)));
        // Access LAZY collection safely inside transaction
        return new ArrayList<>(h.getPatients());
    }

    @Transactional(readOnly = true)
    public List<Hospital> hospitalsOfPatient(Long patientId) {
        Patient p = patientRepo.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient %d not found".formatted(patientId)));
        // Access LAZY collection safely inside transaction
        return new ArrayList<>(p.getHospitals());
    }
}
