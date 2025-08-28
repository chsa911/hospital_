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

@Service @RequiredArgsConstructor
public class BillingService {

    private static final BigDecimal DAILY_RATE = BigDecimal.valueOf(200);

    private final BillRepository billRepo;
    private final PatientRepository patientRepo;
    private final HospitalRepository hospitalRepo;
    private final StayRepository stayRepo;

    public Bill generate(Long patientId, Long hospitalId) {
        var p = patientRepo.findById(patientId).orElseThrow();
        var h = hospitalRepo.findById(hospitalId).orElseThrow();
        List<Stay> stays = stayRepo.findByPatient_Id(patientId).stream()
                .filter(s -> s.getHospital().getId().equals(hospitalId) && !s.isCancelled())
                .toList();

        long days = stays.stream()
                .mapToLong(s -> ChronoUnit.DAYS.between(s.getStartDate(), s.getEndDate()) + 1)
                .sum();

        var bill = Bill.builder().patient(p).hospital(h)
                .amount(DAILY_RATE.multiply(BigDecimal.valueOf(days)))
                .paid(false).build();

        return billRepo.save(bill);
    }

    public List<Bill> billsForPatient(Long patientId) {
        return billRepo.findByPatient_Id(patientId);
    }

    public BigDecimal outstanding(Long patientId, Long hospitalId) {
        return billRepo.findByPatient_IdAndHospital_IdAndPaidFalse(patientId, hospitalId).stream()
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
