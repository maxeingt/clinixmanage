package gt.com.xfactory.dto.request.filter;

import gt.com.xfactory.entity.enums.AppointmentStatus;
import jakarta.ws.rs.QueryParam;

import java.time.LocalDateTime;
import java.util.UUID;

public class MedicalAppointmentFilterDto {

    @QueryParam("doctorId")
    public UUID doctorId;

    @QueryParam("clinicId")
    public UUID clinicId;

    @QueryParam("startDate")
    public LocalDateTime startDate;

    @QueryParam("endDate")
    public LocalDateTime endDate;

    @QueryParam("status")
    public AppointmentStatus status;
}
