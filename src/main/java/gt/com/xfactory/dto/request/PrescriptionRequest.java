package gt.com.xfactory.dto.request;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.*;
import java.util.*;

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

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;

    private LocalDate issueDate;

    private LocalDate expiryDate;
}
