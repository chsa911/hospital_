package com.example.hospital;

import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.Patient;
import com.example.hospital.service.BillingService;
import com.example.hospital.service.HospitalService;
import com.example.hospital.service.PatientService;
import com.example.hospital.service.StayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StayBillingTest {

    @Autowired HospitalService hospitalService;
    @Autowired PatientService patientService;
    @Autowired StayService stayService;
    @Autowired BillingService billingService;

    @Test
    void quarterAndBilling() {
        Hospital h = hospitalService.create("General", "A St");
        Patient p = patientService.create("Bob","Jones");
        hospitalService.registerPatient(p.getId(), h.getId());

        stayService.create(p.getId(), h.getId(), LocalDate.of(2024,1,10), LocalDate.of(2024,1,20)); // 11 days
        stayService.create(p.getId(), h.getId(), LocalDate.of(2024,2,1), LocalDate.of(2024,2,5));   // 5 days

        var bill = billingService.generate(p.getId(), h.getId());
        assertThat(bill.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(200 * 16L));
    }
}
