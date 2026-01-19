package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalAppointmentRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    @NotNull(message = "Clinic ID is required")
    private UUID clinicId;

    private UUID medHistGynecoId;

    @NotNull(message = "Appointment date is required")
    private LocalDateTime appointmentDate;

    private String observation;

    private String medicalHistory;
}
