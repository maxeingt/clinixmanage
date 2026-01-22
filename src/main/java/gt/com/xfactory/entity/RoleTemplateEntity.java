package gt.com.xfactory.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "role_template")
@Getter
@Setter
public class RoleTemplateEntity extends PanacheEntityBase implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

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

    @Column(name = "view_medical_records")
    private Boolean viewMedicalRecords = false;

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
