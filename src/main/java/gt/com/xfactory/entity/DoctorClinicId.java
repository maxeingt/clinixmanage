package gt.com.xfactory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DoctorClinicId implements Serializable {
    @Column(name = "doctor_id", columnDefinition = "CHAR(36)")
    private UUID doctorId;

    @Column(name = "clinic_id", columnDefinition = "CHAR(36)")
    private UUID clinicId;
}
