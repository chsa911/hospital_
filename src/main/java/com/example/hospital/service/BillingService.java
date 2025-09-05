package com.example.hospital.service;

import com.example.hospital.entity.Bill;
import com.example.hospital.entity.Stay;
import com.example.hospital.repository.BillRepository;
import com.example.hospital.repository.PatientRepository;
import com.example.hospital.repository.HospitalRepository;
import com.example.hospital.repository.StayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
@Service
@RequiredArgsConstructor
public class BillingService {

    private static final BigDecimal DAILY_RATE = BigDecimal.valueOf(200);

    private final BillRepository billRepo;
    private final PatientRepository patientRepo;
    private final HospitalRepository hospitalRepo;
    private final StayRepository stayRepo;

    public Bill generate(Long patientId, Long hospitalId) {
        var p = patientRepo.findById(patientId).orElseThrow();
        var h = hospitalRepo.findById(hospitalId).orElseThrow();

        // All non-cancelled stays for this hospital
        var stays = stayRepo.findByPatientId(patientId).stream()
                .filter(s -> s.getHospital().getId().equals(hospitalId) && !s.isCancelled())
                .toList();

        // Inclusive day count per stay
        long totalDays = stays.stream()
                .mapToLong(s -> ChronoUnit.DAYS.between(s.getStartDate(), s.getEndDate()) + 1)
                .sum();

        // Days already billed = sum(previousAmounts) / DAILY_RATE
        BigDecimal billedAmount = billRepo.findByPatientIdAndHospitalId(patientId, hospitalId).stream()
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ensure exact division (amounts are multiples of DAILY_RATE in this model)
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
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
