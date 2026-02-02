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

    private String reason;

    private String diagnosis;

    private String notes;

    private String cancellationReason;

    private UUID followUpAppointmentId;

    private List<AppointmentDiagnosisRequest> diagnoses;
}
