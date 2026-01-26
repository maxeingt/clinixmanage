package gt.com.xfactory.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionMedicationId implements Serializable {

    private UUID prescription;
    private UUID medication;
}
