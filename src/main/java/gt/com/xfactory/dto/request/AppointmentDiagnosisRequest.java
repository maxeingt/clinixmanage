package gt.com.xfactory.dto.request;

import gt.com.xfactory.entity.enums.DiagnosisType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDiagnosisRequest {

    @NotNull(message = "Diagnosis ID is required")
    private UUID diagnosisId;

    @NotNull(message = "Diagnosis type is required")
    private DiagnosisType type;

    private String notes;
}
