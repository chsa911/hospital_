package com.example.hospital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Stay {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) private Patient patient;
    @ManyToOne(optional = false) private Hospital hospital;

    private LocalDate startDate;
    private LocalDate endDate;

    private boolean cancelled;
}
