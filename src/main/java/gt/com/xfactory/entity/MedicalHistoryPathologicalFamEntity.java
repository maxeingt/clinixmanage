package gt.com.xfactory.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

import java.io.*;
import java.time.*;
import java.util.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "medical_history_pathological_fam")
@AllArgsConstructor
@NoArgsConstructor
public class MedicalHistoryPathologicalFamEntity extends PanacheEntityBase implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @Column(name = "medical_history_type", nullable = false, length = 50)
    private String medicalHistoryType;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @TenantId
    @Column(name = "organization_id", nullable = false, columnDefinition = "uuid")
    private String organizationId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
