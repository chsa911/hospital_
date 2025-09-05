package com.example.hospital;

import com.example.hospital.entity.Bill;
import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.Patient;
import com.example.hospital.service.BillingService;
import com.example.hospital.service.HospitalService;
import com.example.hospital.service.PatientService;
import com.example.hospital.service.StayService;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
// Lad nur Repos + DEINE Services (keine Web/gRPC Server!)
@Import({PatientService.class, HospitalService.class, StayService.class, BillingService.class})
// Schließe gRPC-AutoConfigs sicherheitshalber aus:
@ImportAutoConfiguration(exclude = {
        GrpcServerAutoConfiguration.class,
        GrpcServerFactoryAutoConfiguration.class
})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // In-Memory DB + Schema
        "spring.datasource.url=jdbc:h2:mem:hospitaldb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        // Defensiv: keinerlei Ports binden
        "server.port=0",
        "grpc.server.port=0",
        // Optional: ganz ohne Web-Kontext
        "spring.main.web-application-type=none"
})
class BillingOutstandingTest {

    @Autowired HospitalService hospitalService;
    @Autowired PatientService patientService;
    @Autowired StayService stayService;
    @Autowired BillingService billingService;

    @Test
    void outstandingBalanceIncludesUnpaidBills() {
        Hospital h = hospitalService.create("General", "A St");
        Patient p = patientService.create("Dora", "Fischer");
        hospitalService.registerPatient(p.getId(), h.getId());

        // 10 Tage => 2000
        stayService.create(p.getId(), h.getId(),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 10));
        Bill b1 = billingService.generate(p.getId(), h.getId());
        assertThat(b1.getPaid()).isFalse();

        // 5 Tage => 1000
        stayService.create(p.getId(), h.getId(),
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 5));
        Bill b2 = billingService.generate(p.getId(), h.getId());
        assertThat(b2.getPaid()).isFalse();

        assertThat(billingService.outstanding(p.getId(), h.getId()))
                .isEqualByComparingTo(BigDecimal.valueOf(3000));
    }
}
