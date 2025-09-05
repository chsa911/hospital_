package com.example.hospital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Hospital {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ToString.Include
    private String name;

    private String address;

    // Patients registered (ManyToMany)
    @ManyToMany
    @JoinTable(
            name = "patient_hospital",
            joinColumns = @JoinColumn(name = "hospital_id"),
            inverseJoinColumns = @JoinColumn(name = "patient_id")
    )
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Patient> patients = new HashSet<>();

    // Convenience to keep both sides in sync
    public void addPatient(Patient p) {
        patients.add(p);
        p.getHospitals().add(this);
    }
    public void removePatient(Patient p) {
        patients.remove(p);
        p.getHospitals().remove(this);
    }
}
