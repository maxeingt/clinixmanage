package gt.com.xfactory.dto.request.filter;

import jakarta.ws.rs.*;
import lombok.*;

import java.time.*;
import java.util.*;

@Data
public class MedicalRecordFilterDto {

    @QueryParam("patientId")
    public UUID patientId;

    @QueryParam("doctorId")
    public UUID doctorId;

    @QueryParam("specialtyId")
    public UUID specialtyId;

    @QueryParam("startDate")
    public LocalDateTime startDate;

    @QueryParam("endDate")
    public LocalDateTime endDate;
}
