package gt.com.xfactory.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

import java.util.*;

@Entity
@Table(name = "doctor_specialty")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSpecialtyEntity extends PanacheEntityBase {

    @EmbeddedId
    private DoctorSpecialtyId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("doctorId")
    @JoinColumn(name = "doctor_id")
    private DoctorEntity doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("specialtyId")
    @JoinColumn(name = "specialty_id")
    private SpecialtyEntity specialty;

    @TenantId
    @Column(name = "organization_id", nullable = false, columnDefinition = "uuid")
    private String organizationId;
}
