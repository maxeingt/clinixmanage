package gt.com.xfactory.dto.request.filter;

import jakarta.ws.rs.QueryParam;

import java.util.UUID;

public class MedicalAppointmentFilterDto {

    @QueryParam("doctorId")
    public UUID doctorId;

    @QueryParam("clinicId")
    public UUID clinicId;
}
