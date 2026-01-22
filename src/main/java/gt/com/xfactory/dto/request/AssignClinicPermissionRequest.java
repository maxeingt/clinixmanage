package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignClinicPermissionRequest {
    @NotNull(message = "Clinic ID is required")
    private UUID clinicId;

    private Boolean adminPatients = false;
    private Boolean adminDoctors = false;
    private Boolean adminAppointments = false;
    private Boolean adminClinics = false;
    private Boolean adminUsers = false;
    private Boolean adminSpecialties = false;
    private Boolean manageAssignments = false;
    private Boolean viewMedicalRecords = false;

    // Opcionalmente, asignar usando una plantilla de rol
    private UUID roleTemplateId;
}
