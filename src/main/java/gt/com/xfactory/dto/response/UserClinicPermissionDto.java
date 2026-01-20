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
public class UserClinicPermissionDto implements Serializable {
    private UUID userId;
    private UUID clinicId;
    private String clinicName;
    private Boolean adminPatients;
    private Boolean adminDoctors;
    private Boolean adminAppointments;
    private Boolean adminClinics;
    private Boolean adminUsers;
    private Boolean adminSpecialties;
    private Boolean manageAssignments;
    private LocalDateTime createdAt;
}
