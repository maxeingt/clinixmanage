package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorRequest {
    @NotBlank(message = "First name is required")
    @Size(max = 150, message = "First name must not exceed 150 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 150, message = "Last name must not exceed 150 characters")
    private String lastName;

    private LocalDate birthdate;

    @Size(max = 250, message = "Address must not exceed 250 characters")
    private String address;

    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String mail;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
}
