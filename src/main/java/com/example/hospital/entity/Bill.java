package com.example.hospital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Bill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) private Patient patient;
    @ManyToOne(optional = false) private Hospital hospital;

    private BigDecimal amount;
    private Boolean paid;
}
