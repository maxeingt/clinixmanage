package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionMedicationRequest {

    @NotNull(message = "Medication ID is required")
    private UUID medicationId;

    @NotBlank(message = "Dose is required")
    @Size(max = 100, message = "Dose must not exceed 100 characters")
    private String dose;

    @NotBlank(message = "Frequency is required")
    @Size(max = 100, message = "Frequency must not exceed 100 characters")
    private String frequency;

    @Size(max = 100, message = "Duration must not exceed 100 characters")
    private String duration;

    private Integer quantity;

    @Size(max = 100, message = "Administration route must not exceed 100 characters")
    private String administrationRoute;

    @Size(max = 5000, message = "Specific indications must not exceed 5000 characters")
    private String specificIndications;
}
