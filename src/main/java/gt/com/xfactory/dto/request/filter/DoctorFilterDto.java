package gt.com.xfactory.dto.request.filter;

import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import lombok.*;

@Data
public class DoctorFilterDto {
    @QueryParam("firstName")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s'-]{1,50}$", message = "First name contains invalid characters.")
    public String firstName;

    @QueryParam("lastName")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s'-]{1,50}$", message = "Last name contains invalid characters.")
    public String lastName;

    @QueryParam("mail")
    @Email(message = "Invalid email format.")
    public String mail;
}
