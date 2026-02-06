package gt.com.xfactory.controller;

import gt.com.xfactory.dto.response.NotificationDto;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.repository.*;
import gt.com.xfactory.service.impl.NotificationService;
import io.quarkus.security.identity.*;
import io.smallrye.mutiny.Multi;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.*;
import org.jboss.resteasy.reactive.RestStreamElementType;

import java.util.UUID;

@RequestScoped
@Path("/api/v1/notifications")
@RolesAllowed({"admin", "doctor"})
public class NotificationController {

    @Inject
    NotificationService notificationService;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @Inject
    DoctorRepository doctorRepository;

    @GET
    @Path("/stream/{doctorId}")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<NotificationDto> stream(@PathParam("doctorId") UUID doctorId) {
        if (!securityIdentity.hasRole("admin")) {
            String keycloakId = jwt.getSubject();
            UUID currentDoctorId = doctorRepository.findByUserKeycloakId(keycloakId)
                    .map(DoctorEntity::getId)
                    .orElseThrow(() -> new ForbiddenException("Doctor no encontrado para el usuario actual"));
            if (!doctorId.equals(currentDoctorId)) {
                throw new ForbiddenException("No tiene acceso a las notificaciones de otro doctor");
            }
        }
        return notificationService.register(doctorId);
    }
}
