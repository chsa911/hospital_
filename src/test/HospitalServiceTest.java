package com.example.hospital;

import com.example.hospital.entity.Hospital;
import com.example.hospital.service.HospitalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HospitalServiceTest {

    @Autowired HospitalService svc;

    @Test
    void createAndList() {
        svc.create("City Hospital", "123 Main");
        assertThat(svc.list()).extracting(Hospital::getName).contains("City Hospital");
    }
}
