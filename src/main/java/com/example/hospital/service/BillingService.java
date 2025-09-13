package com.example.hospital.service;

import com.example.hospital.entity.Bill;
import com.example.hospital.entity.Stay;
import com.example.hospital.repository.BillRepository;
import com.example.hospital.repository.PatientRepository;
import com.example.hospital.repository.HospitalRepository;
import com.example.hospital.repository.StayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BillingService {

    // Keep this consistent with your quarter summary rate
    private static final BigDecimal DAILY_RATE = new BigDecimal("200.00");

    private final BillRepository billRepo;
    private final PatientRepository patientRepo;
    private final HospitalRepository hospitalRepo;
    private final StayRepository stayRepo;

    @Transactional
    public Bill generate(Long patientId, Long hospitalId) {
        var p = patientRepo.findById(patientId)
                .orElseThrow(() -> new NoSuchElementException("Patient " + patientId + " not found"));
        var h = hospitalRepo.findById(hospitalId)
                .orElseThrow(() -> new NoSuchElementException("Hospital " + hospitalId + " not found"));

        // All completed stays (have start & end date) for this (patient, hospital)
        List<Stay> stays = stayRepo.findByPatientId(patientId).stream()
                .filter(s -> s != null && s.getStartDate() != null && s.getEndDate() != null)
                .filter(s -> s.getHospital() != null && Objects.equals(s.getHospital().getId(), hospitalId))
                .toList();

        // Inclusive day count per stay
        long totalDays = stays.stream()
                .mapToLong(s -> ChronoUnit.DAYS.between(s.getStartDate(), s.getEndDate()) + 1)
                .sum();

        // Sum previous billed amount for this (patient, hospital)
        BigDecimal billedAmount = billRepo.findByPatientIdAndHospitalId(patientId, hospitalId).stream()
                .map(Bill::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Convert previous amount to days; exact division required by your model
        long billedDays = billedAmount.divide(DAILY_RATE).longValueExact();

        long unbilledDays = totalDays - billedDays;
        if (unbilledDays <= 0) {
            throw new IllegalStateException("No unbilled stays to generate a bill for.");
        }

        var bill = Bill.builder()
                .patient(p)
                .hospital(h)
                .amount(DAILY_RATE.multiply(BigDecimal.valueOf(unbilledDays)))
                .paid(false)
                .build();

        return billRepo.save(bill);
    }

    public List<Bill> billsForPatient(Long patientId) {
        return billRepo.findByPatientId(patientId);
    }

    public BigDecimal outstanding(Long patientId, Long hospitalId) {
        return billRepo.findByPatientIdAndHospitalIdAndPaidFalse(patientId, hospitalId).stream()
                .map(Bill::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
