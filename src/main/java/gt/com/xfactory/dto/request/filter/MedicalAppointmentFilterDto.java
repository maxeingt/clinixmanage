package gt.com.xfactory.dto.request.filter;

import gt.com.xfactory.entity.enums.*;
import jakarta.ws.rs.*;
import lombok.*;

import java.time.*;
import java.util.*;

@Data
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
