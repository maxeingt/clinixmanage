package gt.com.xfactory.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;

import java.time.*;
import java.util.*;

@Audited
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

    @TenantId
    @Column(name = "organization_id", nullable = false, columnDefinition = "uuid")
    private String organizationId;
}
