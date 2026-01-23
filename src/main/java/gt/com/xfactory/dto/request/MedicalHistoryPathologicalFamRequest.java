package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalHistoryPathologicalFamRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @NotBlank(message = "Medical history type is required")
    private String medicalHistoryType;

    @NotBlank(message = "Description is required")
    private String description;
}
