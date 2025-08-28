package com.example.hospital;

import com.example.hospital.entity.Patient;
import com.example.hospital.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PatientServiceTest {
    @Autowired PatientService svc;

    @Test
    void createUpdateDeletePatient() {
        Patient p = svc.create("Alice","Smith");
        assertThat(p.getId()).isNotNull();
        p = svc.update(p.getId(),"Alicia","Smith");
        assertThat(p.getFirstName()).isEqualTo("Alicia");
        svc.delete(p.getId());
    }
}
