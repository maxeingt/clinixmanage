package gt.com.xfactory.dto.request;

import gt.com.xfactory.entity.enums.MedicalRecordType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    private UUID appointmentId;

    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    private UUID specialtyId;

    private MedicalRecordType recordType;

    private String chiefComplaint;

    private String presentIllness;

    private String physicalExam;

    private String treatmentPlan;

    private Map<String, Object> vitalSigns;

    private Map<String, Object> specialtyData;

    private Object attachments;
}
