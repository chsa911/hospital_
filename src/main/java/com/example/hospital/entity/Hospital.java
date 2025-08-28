package com.example.hospital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Hospital {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;

    // Patients registered (ManyToMany)
    @ManyToMany
    @JoinTable(name = "patient_hospital",
            joinColumns = @JoinColumn(name = "hospital_id"),
            inverseJoinColumns = @JoinColumn(name = "patient_id"))
    @Builder.Default
    private Set<Patient> patients = new HashSet<>();
}
