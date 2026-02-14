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
    @Size(max = 150, message = "First name must not exceed 150 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 150, message = "Last name must not exceed 150 characters")
    private String lastName;

    @NotNull(message = "Birthdate is required")
    private LocalDate birthdate;

    @Size(max = 20, message = "Gender must not exceed 20 characters")
    private String gender;

    @Size(max = 10, message = "Blood group must not exceed 10 characters")
    private String bloodGroup;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Size(max = 250, message = "Address must not exceed 250 characters")
    private String address;

    @Size(max = 75, message = "Marital status must not exceed 75 characters")
    private String maritalStatus;

    @Size(max = 150, message = "Occupation must not exceed 150 characters")
    private String occupation;

    @Size(max = 200, message = "Emergency contact name must not exceed 200 characters")
    private String emergencyContactName;

    @Size(max = 20, message = "Emergency contact phone must not exceed 20 characters")
    private String emergencyContactPhone;

    @Size(max = 5000, message = "Allergies must not exceed 5000 characters")
    private String allergies;

    @Size(max = 5000, message = "Chronic conditions must not exceed 5000 characters")
    private String chronicConditions;

    @Size(max = 150, message = "Insurance provider must not exceed 150 characters")
    private String insuranceProvider;

    @Size(max = 50, message = "Insurance number must not exceed 50 characters")
    private String insuranceNumber;

    @Size(max = 20, message = "DPI must not exceed 20 characters")
    private String dpi;

    @Size(max = 100, message = "Nationality must not exceed 100 characters")
    private String nationality;

    private BigDecimal height;
    private BigDecimal weight;
}
