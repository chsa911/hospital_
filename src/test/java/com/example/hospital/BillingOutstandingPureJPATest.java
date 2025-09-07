package com.example.hospital;

import com.example.hospital.entity.Bill;
import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.Patient;
import com.example.hospital.repository.BillRepository;
import com.example.hospital.repository.HospitalRepository;
import com.example.hospital.repository.PatientRepository;
import com.example.hospital.repository.StayRepository;
import com.example.hospital.service.BillingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure JPA slice; no web, no gRPC, no auto-config surprises.
 * We manually instantiate the service under test.
 */
@DataJpaTest(showSql = false)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:hospitaldb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.main.web-application-type=none",
        // ⬇️ prevent auto SQL scripts and Flyway in this test
        "spring.sql.init.mode=never",
        "spring.flyway.enabled=false"
})
class BillingOutstandingPureJpaTest {

    @Autowired PatientRepository patientRepo;
    @Autowired HospitalRepository hospitalRepo;
    @Autowired BillRepository billRepo;
    @Autowired StayRepository stayRepo;

    @Test
    void outstandingBalance_sumsUnpaidOnly() {
        // Manually construct the service (no Spring bean needed)
        BillingService billingService = new BillingService(billRepo, patientRepo, hospitalRepo, stayRepo);

        var h = hospitalRepo.save(Hospital.builder().name("General").address("A St").build());
        var p = patientRepo.save(Patient.builder().firstName("Dora").lastName("Fischer").build());

        // two UNPAID bills -> 2000 + 1000
        billRepo.save(Bill.builder().patient(p).hospital(h).amount(new BigDecimal("2000")).paid(false).build());
        billRepo.save(Bill.builder().patient(p).hospital(h).amount(new BigDecimal("1000")).paid(false).build());

        // one PAID bill -> must be ignored
        billRepo.save(Bill.builder().patient(p).hospital(h).amount(new BigDecimal("9999")).paid(true).build());

        assertThat(billingService.outstanding(p.getId(), h.getId()))
                .isEqualByComparingTo(new BigDecimal("3000"));
    }
}
