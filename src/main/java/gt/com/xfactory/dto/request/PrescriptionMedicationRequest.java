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
public class PrescriptionMedicationRequest {

    @NotNull(message = "Medication ID is required")
    private UUID medicationId;

    @NotBlank(message = "Dose is required")
    private String dose;

    @NotBlank(message = "Frequency is required")
    private String frequency;

    private String duration;

    private Integer quantity;

    private String administrationRoute;

    private String specificIndications;
}
