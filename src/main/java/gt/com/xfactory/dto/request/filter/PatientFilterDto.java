package gt.com.xfactory.dto.request.filter;

import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import lombok.*;

import java.time.*;

@Data
public class PatientFilterDto {
    @QueryParam("name")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s'-]{1,50}$", message = "Name contains invalid characters.")
    public String name;

    @QueryParam("phone")
    @Pattern(regexp = "^[0-9\\s+-]{1,20}$", message = "Phone contains invalid characters.")
    public String phone;

    @QueryParam("maritalStatus")
    public String maritalStatus;

    @QueryParam("appointmentDateFrom")
    public LocalDateTime appointmentDateFrom;

    @QueryParam("appointmentDateTo")
    public LocalDateTime appointmentDateTo;
}
