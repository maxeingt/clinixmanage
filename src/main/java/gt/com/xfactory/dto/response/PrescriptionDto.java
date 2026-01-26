package gt.com.xfactory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDto implements Serializable {
    private UUID id;
    private UUID medicalRecordId;
    private UUID patientId;
    private String patientName;
    private UUID doctorId;
    private String doctorName;
    private List<PrescriptionMedicationDto> medications;
    private String notes;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private LocalDateTime createdAt;
}
