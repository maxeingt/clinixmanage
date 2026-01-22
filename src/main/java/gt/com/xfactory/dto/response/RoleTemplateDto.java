package gt.com.xfactory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleTemplateDto {
    private UUID id;
    private String name;
    private String description;
    private Boolean adminPatients;
    private Boolean adminDoctors;
    private Boolean adminAppointments;
    private Boolean adminClinics;
    private Boolean adminUsers;
    private Boolean adminSpecialties;
    private Boolean manageAssignments;
    private Boolean viewMedicalRecords;
}
