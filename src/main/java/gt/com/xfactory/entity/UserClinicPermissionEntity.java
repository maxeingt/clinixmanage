package gt.com.xfactory.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_clinic_permission")
@Getter
@Setter
public class UserClinicPermissionEntity extends PanacheEntityBase {

    @EmbeddedId
    private UserClinicPermissionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clinicId")
    @JoinColumn(name = "clinic_id")
    private ClinicEntity clinic;

    @Column(name = "admin_patients")
    private Boolean adminPatients = false;

    @Column(name = "admin_doctors")
    private Boolean adminDoctors = false;

    @Column(name = "admin_appointments")
    private Boolean adminAppointments = false;

    @Column(name = "admin_clinics")
    private Boolean adminClinics = false;

    @Column(name = "admin_users")
    private Boolean adminUsers = false;

    @Column(name = "admin_specialties")
    private Boolean adminSpecialties = false;

    @Column(name = "manage_assignments")
    private Boolean manageAssignments = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
