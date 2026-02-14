package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalHistoryPathologicalFamRequest {

    private UUID patientId;

    @NotBlank(message = "Medical history type is required")
    @Size(max = 50, message = "Medical history type must not exceed 50 characters")
    private String medicalHistoryType;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
}
