package gt.com.xfactory.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "prescription_medication")
@IdClass(PrescriptionMedicationId.class)
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionMedicationEntity extends PanacheEntityBase implements Serializable {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private PrescriptionEntity prescription;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private MedicationEntity medication;

    @Column(name = "dose", nullable = false, length = 100)
    private String dose;

    @Column(name = "frequency", nullable = false, length = 100)
    private String frequency;

    @Column(name = "duration", length = 100)
    private String duration;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "administration_route", length = 100)
    private String administrationRoute;

    @Column(name = "specific_indications", columnDefinition = "TEXT")
    private String specificIndications;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
