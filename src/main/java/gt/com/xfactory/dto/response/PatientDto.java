package gt.com.xfactory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDto implements Serializable {
    private UUID id;
    private String firstName;
    private String lastName;
    private LocalDate birthdate;
    private int age;
    private String gender;
    private String bloodGroup;
    private String phone;
    private String email;
    private String address;
    private String maritalStatus;
    private String occupation;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String allergies;
    private String chronicConditions;
    private String insuranceProvider;
    private String insuranceNumber;
}
