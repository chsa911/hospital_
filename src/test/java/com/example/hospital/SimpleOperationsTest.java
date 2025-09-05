
package com.example.hospital;

import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.Patient;
import com.example.hospital.entity.Bill;
import com.example.hospital.service.HospitalService;
import com.example.hospital.service.PatientService;
import com.example.hospital.service.StayService;
import com.example.hospital.service.BillingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
class SimpleOperationsTest {

    @Autowired PatientService patientService;
    @Autowired HospitalService hospitalService;
    @Autowired StayService stayService;
    @Autowired BillingService billingService;

    @Test
    void createPatient_andHospital_thenRegister() {
        Patient p = patientService.create("Alice", "Meyer");
        Hospital h = hospitalService.create("City Hospital", "Main Street 1");

        // should not throw:
        hospitalService.registerPatient(p.getId(), h.getId());

        // at least verify IDs were generated
        assertThat(p.getId()).isNotNull();
        assertThat(h.getId()).isNotNull();
    }

    @Test
    void createTwoStays_andGenerateBill_amountMatchesDailyRate() {
        Hospital h = hospitalService.create("General", "A St");
        Patient p = patientService.create("Bob", "Jones");
        hospitalService.registerPatient(p.getId(), h.getId());

        // Example stays: 2024-01-10..2024-01-20 (inclusive?) and 2024-02-01..2024-02-05
        // Existing project tests imply 200 per day rate and 16 total days.
        stayService.create(p.getId(), h.getId(), LocalDate.of(2024,1,10), LocalDate.of(2024,1,20)); // 11 days
        stayService.create(p.getId(), h.getId(), LocalDate.of(2024,2,1), LocalDate.of(2024,2,5));   // 5 days

        Bill bill = billingService.generate(p.getId(), h.getId());
        assertThat(bill.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(200L * 16L));
    }

    @Test
    void createPatient_thenFetchById_returnsSame() {
        Patient created = patientService.create("Clara", "Schulz");
        Patient loaded = patientService.get(created.getId());
        assertThat(loaded.getFirstName()).isEqualTo("Clara");
        assertThat(loaded.getLastName()).isEqualTo("Schulz");
    }
}
