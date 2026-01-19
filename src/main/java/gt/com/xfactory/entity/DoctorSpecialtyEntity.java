package gt.com.xfactory.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
}
