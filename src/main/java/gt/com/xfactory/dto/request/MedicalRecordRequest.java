package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.*;

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

    @Size(max = 10000, message = "Chief complaint must not exceed 10000 characters")
    private String chiefComplaint;

    @Size(max = 10000, message = "Present illness must not exceed 10000 characters")
    private String presentIllness;

    @Size(max = 10000, message = "Physical exam must not exceed 10000 characters")
    private String physicalExam;

    @Size(max = 10000, message = "Treatment plan must not exceed 10000 characters")
    private String treatmentPlan;

    private Map<String, Object> vitalSigns;

    private Map<String, Object> specialtyData;

    private UUID formTemplateId;

    private Object attachments;
}
