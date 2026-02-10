package gt.com.xfactory.controller;

import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.service.impl.*;
import io.smallrye.mutiny.*;
import jakarta.annotation.security.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.reactive.*;

import java.util.*;

@RequestScoped
@Path("/api/v1/notifications")
@RolesAllowed({"admin", "doctor"})
public class NotificationController {

    @Inject
    NotificationService notificationService;

    @Inject
    SecurityContextService securityContext;

    @GET
    @Path("/stream/{doctorId}")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<NotificationDto> stream(@PathParam("doctorId") UUID doctorId) {
        securityContext.validateOwnDoctorAccess(doctorId);
        return notificationService.register(doctorId);
    }
}
