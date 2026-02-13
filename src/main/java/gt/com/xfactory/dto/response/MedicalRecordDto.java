package gt.com.xfactory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordDto implements Serializable {
    private UUID id;
    private UUID patientId;
    private String patientName;
    private UUID appointmentId;
    private UUID specialtyId;
    private String specialtyName;
    private UUID doctorId;
    private String doctorName;
    private String recordType;
    private String chiefComplaint;
    private String presentIllness;
    private String physicalExam;
    private String treatmentPlan;
    private Map<String, Object> vitalSigns;
    private Map<String, Object> specialtyData;
    private UUID formTemplateId;
    private Integer formTemplateVersion;
    private Object attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
