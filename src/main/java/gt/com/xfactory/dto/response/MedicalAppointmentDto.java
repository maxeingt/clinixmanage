package gt.com.xfactory.dto.response;

import lombok.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalAppointmentDto implements Serializable {
    private UUID id;
    private UUID patientId;
    private String patientName;
    private UUID doctorId;
    private String doctorName;
    private UUID clinicId;
    private String clinicName;
    private UUID specialtyId;
    private String specialtyName;
    private String status;
    private LocalDateTime appointmentDate;
    private String reason;
    private String diagnosis;
    private String notes;
    private LocalDateTime checkInTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String cancellationReason;
    private String source;
    private UUID followUpAppointmentId;
    private UUID childFollowUpId;
    private List<AppointmentDiagnosisDto> diagnoses;
    private LocalDateTime createdAt;
}
