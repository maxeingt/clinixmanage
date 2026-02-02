package gt.com.xfactory.dto.request.filter;

import gt.com.xfactory.entity.enums.*;
import jakarta.ws.rs.*;

import java.time.*;
import java.util.*;

public class LabOrderFilterDto {

    @QueryParam("patientId")
    public UUID patientId;

    @QueryParam("doctorId")
    public UUID doctorId;

    @QueryParam("status")
    public LabOrderStatus status;

    @QueryParam("startDate")
    public LocalDateTime startDate;

    @QueryParam("endDate")
    public LocalDateTime endDate;
}
