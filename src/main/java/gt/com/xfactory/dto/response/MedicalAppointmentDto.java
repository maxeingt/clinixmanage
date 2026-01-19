package gt.com.xfactory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalAppointmentDto implements Serializable {
    private UUID id;
    private UUID patientId;
    private UUID doctorId;
    private UUID clinicId;
    private UUID medHistGynecoId;
    private LocalDateTime appointmentDate;
    private String observation;
    private String medicalHistory;
}
