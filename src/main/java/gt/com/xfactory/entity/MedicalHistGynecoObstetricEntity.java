package gt.com.xfactory.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
@Table(name = "medical_hist_gyneco_obstetric")
@AllArgsConstructor
@NoArgsConstructor
public class MedicalHistGynecoObstetricEntity extends PanacheEntityBase implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @Column(name = "medical_history_type", nullable = false, length = 50)
    private String medicalHistoryType;

    @Column(name = "last_menstrual_period")
    private LocalDate lastMenstrualPeriod;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "height", nullable = false)
    private Double height;

    @Column(name = "duration")
    private Double duration;

    @Column(name = "cycles", nullable = false)
    private Double cycles;

    @Column(name = "reliable", length = 10)
    private String reliable;

    @Column(name = "papanicolaou", length = 150)
    private String papanicolaou;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
