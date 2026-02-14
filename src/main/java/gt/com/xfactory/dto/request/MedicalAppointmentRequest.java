package gt.com.xfactory.dto.request;

import gt.com.xfactory.entity.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalAppointmentRequest {

    private UUID patientId;

    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    @NotNull(message = "Clinic ID is required")
    private UUID clinicId;

    private UUID specialtyId;

    private AppointmentStatus status;

    @NotNull(message = "Appointment date is required")
    private LocalDateTime appointmentDate;

    @Size(max = 5000, message = "Reason must not exceed 5000 characters")
    private String reason;

    @Size(max = 5000, message = "Diagnosis must not exceed 5000 characters")
    private String diagnosis;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;

    @Size(max = 1000, message = "Cancellation reason must not exceed 1000 characters")
    private String cancellationReason;

    private UUID followUpAppointmentId;

    private List<AppointmentDiagnosisRequest> diagnoses;
}
