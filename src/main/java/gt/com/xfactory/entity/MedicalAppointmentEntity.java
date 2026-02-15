package gt.com.xfactory.entity;

import gt.com.xfactory.entity.enums.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;
import org.hibernate.envers.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Getter
@Setter
@ToString
@Audited
@Entity
@Table(name = "medical_appointment")
@AllArgsConstructor
@NoArgsConstructor
public class MedicalAppointmentEntity extends PanacheEntityBase implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorEntity doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private ClinicEntity clinic;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id")
    private SpecialtyEntity specialty;

    @Column(name = "status", nullable = false)
    private AppointmentStatus status = AppointmentStatus.scheduled;

    @Column(name = "appointment_date", nullable = false)
    private LocalDateTime appointmentDate;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "notified_30_min")
    private boolean notified30Min;

    @Column(name = "notified_10_min")
    private boolean notified10Min;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "source", length = 20)
    private AppointmentSource source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follow_up_appointment_id")
    private MedicalAppointmentEntity followUpAppointment;

    @NotAudited
    @OneToMany(mappedBy = "appointment", fetch = FetchType.LAZY)
    private List<AppointmentDiagnosisEntity> diagnoses;

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
        if (status == null) {
            status = AppointmentStatus.scheduled;
        }
        if (source == null) {
            source = AppointmentSource.web;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
