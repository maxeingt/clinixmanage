package gt.com.xfactory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalHistoryPathologicalFamDto implements Serializable {
    private UUID id;
    private UUID patientId;
    private String medicalHistoryType;
    private String description;
}
