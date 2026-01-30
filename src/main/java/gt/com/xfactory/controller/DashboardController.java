package gt.com.xfactory.controller;

import gt.com.xfactory.dto.response.DashboardDto;
import gt.com.xfactory.service.impl.DashboardService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@RequestScoped
@Path("/api/v1/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin", "doctor", "secretary"})
public class DashboardController {

    @Inject
    DashboardService dashboardService;

    @GET
    public DashboardDto getDashboard(@QueryParam("clinicId") UUID clinicId,
                                     @QueryParam("doctorId") UUID doctorId) {
        return dashboardService.getDashboardMetrics(clinicId, doctorId);
    }
}
