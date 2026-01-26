package gt.com.xfactory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    private UUID medicalRecordId;

    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    @NotNull(message = "Medications list is required")
    @Valid
    private List<PrescriptionMedicationRequest> medications;

    private String notes;

    private LocalDate issueDate;

    private LocalDate expiryDate;
}
