package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.service.impl.*;
import jakarta.annotation.security.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.*;

@RequestScoped
@Path("/api/v1/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin", "doctor", "secretary"})
public class DashboardController {

    @Inject
    DashboardService dashboardService;

    @Inject
    DashboardWidgetService dashboardWidgetService;

    @GET
    public DashboardDto getDashboard(@QueryParam("clinicId") UUID clinicId,
                                     @QueryParam("doctorId") UUID doctorId) {
        return dashboardService.getDashboardMetrics(clinicId, doctorId);
    }

    @GET
    @Path("/widgets/config")
    public WidgetConfigDto getWidgetConfig(@QueryParam("clinicId") UUID clinicId) {
        return dashboardWidgetService.getWidgetConfig(clinicId);
    }

    @PUT
    @Path("/widgets/config")
    @Consumes(MediaType.APPLICATION_JSON)
    public WidgetConfigDto saveWidgetConfig(@Valid WidgetConfigRequest request) {
        return dashboardWidgetService.saveWidgetConfig(request);
    }

    @GET
    @Path("/widgets")
    public DashboardWidgetsDto getWidgets(@QueryParam("clinicId") UUID clinicId,
                                          @QueryParam("doctorId") UUID doctorId) {
        return dashboardWidgetService.getWidgets(clinicId, doctorId);
    }
}
