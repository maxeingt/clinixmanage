package gt.com.xfactory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSpecialtyId implements Serializable {

    @Column(name = "doctor_id", columnDefinition = "CHAR(36)")
    private UUID doctorId;

    @Column(name = "specialty_id", columnDefinition = "CHAR(36)")
    private UUID specialtyId;
}
