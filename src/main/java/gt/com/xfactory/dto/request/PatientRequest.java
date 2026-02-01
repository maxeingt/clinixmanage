package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.*;
import java.time.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Birthdate is required")
    private LocalDate birthdate;

    private String gender;
    private String bloodGroup;
    private String phone;

    @Email(message = "Invalid email format")
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
    private String dpi;
    private String nationality;
    private BigDecimal height;
    private BigDecimal weight;
}
