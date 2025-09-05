package com.example.hospital;

import com.example.hospital.entity.Bill;
import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.Patient;
import com.example.hospital.service.BillingService;
import com.example.hospital.service.HospitalService;
import com.example.hospital.service.PatientService;
import com.example.hospital.service.StayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lean, stable tests: only JPA + your services. No web/grpc server ports.
 */
@DataJpaTest(showSql = false)
@Import({PatientService.class, HospitalService.class, StayService.class, BillingService.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:hospitaldb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "server.port=0",
        "grpc.server.port=0"
})
class SimpleOperationsJpaTest {

    @Autowired PatientService patientService;
    @Autowired HospitalService hospitalService;
    @Autowired StayService stayService;
    @Autowired BillingService billingService;

    @Test
    void createPatient_andHospital_thenRegister() {
        Patient p = patientService.create("Alice", "Meyer");
        Hospital h = hospitalService.create("City Hospital", "Main Street 1");
        hospitalService.registerPatient(p.getId(), h.getId());

        assertThat(p.getId()).isNotNull();
        assertThat(h.getId()).isNotNull();
    }

    @Test
    void createPatient_thenFetchById_returnsSame() {
        Patient created = patientService.create("Clara", "Schulz");
        Patient loaded = patientService.get(created.getId());
        assertThat(loaded.getFirstName()).isEqualTo("Clara");
        assertThat(loaded.getLastName()).isEqualTo("Schulz");
    }

    @Test
    void createTwoStays_andGenerateBill_amountMatchesDailyRate() {
        Hospital h = hospitalService.create("General", "A St");
        Patient p = patientService.create("Bob", "Jones");
        hospitalService.registerPatient(p.getId(), h.getId());

        stayService.create(p.getId(), h.getId(), LocalDate.of(2024,1,10), LocalDate.of(2024,1,20)); // 11 days
        stayService.create(p.getId(), h.getId(), LocalDate.of(2024,2,1), LocalDate.of(2024,2,5));   // 5 days

        Bill bill = billingService.generate(p.getId(), h.getId());
        assertThat(bill.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(200L * 16L));
    }
}
