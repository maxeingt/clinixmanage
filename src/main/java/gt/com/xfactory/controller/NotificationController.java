package gt.com.xfactory.controller;

import gt.com.xfactory.dto.response.NotificationDto;
import gt.com.xfactory.service.impl.NotificationService;
import io.smallrye.mutiny.Multi;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestStreamElementType;

import java.util.UUID;

@RequestScoped
@Path("/api/v1/notifications")
@RolesAllowed({"user", "admin", "doctor", "secretary"})
public class NotificationController {

    @Inject
    NotificationService notificationService;

    @GET
    @Path("/stream/{doctorId}")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<NotificationDto> stream(@PathParam("doctorId") UUID doctorId) {
        return notificationService.register(doctorId);
    }
}
