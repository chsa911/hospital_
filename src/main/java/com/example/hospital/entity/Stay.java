// Stay.java
package com.example.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;



@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(indexes = {
        @Index(name = "idx_stay_patient_start", columnList = "patient_id,startDate"),
        @Index(name = "idx_stay_hospital",      columnList = "hospital_id"),
        @Index(name = "idx_stay_status",        columnList = "status")
})
public class Stay {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_stay_patient"))
    private Patient patient;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_stay_hospital"))
    private Hospital hospital;

    @Column(nullable = false)
    private LocalDate startDate;

    // null while ongoing; set when discharged
    private LocalDate endDate;

    // 🔹 Put StayStatus here (replaces the old boolean 'cancelled')
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StayStatus status;

    @Column(length = 200)
    private String cancellationReason;

    @PrePersist
    void prePersist() {
        if (status == null) status = StayStatus.ACTIVE;
    }

    public void complete(LocalDate end) {
        this.endDate = end;
        this.status = StayStatus.COMPLETED;
    }

    public void cancel(String reason) {
        this.status = StayStatus.CANCELLED;
        this.cancellationReason = reason;
    }
}
