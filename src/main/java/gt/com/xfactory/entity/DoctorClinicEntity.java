package gt.com.xfactory.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_clinic")
@Getter
@Setter
public class DoctorClinicEntity extends PanacheEntityBase {

    @EmbeddedId
    private DoctorClinicId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("doctorId")
    @JoinColumn(name = "doctor_id")
    private DoctorEntity doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clinicId")
    @JoinColumn(name = "clinic_id")
    private ClinicEntity clinic;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "unassigned_at")
    private LocalDateTime unassignedAt;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
