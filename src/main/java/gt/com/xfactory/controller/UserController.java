package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.ChangePasswordRequest;
import gt.com.xfactory.dto.request.UserRequest;
import gt.com.xfactory.dto.response.UserDto;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.repository.*;
import gt.com.xfactory.service.impl.UserService;
import io.quarkus.security.identity.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.*;

import java.util.List;
import java.util.UUID;

@RequestScoped
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "doctor", "secretary"})
public class UserController {

    @Inject
    UserService userService;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @Inject
    UserRepository userRepository;

    @GET
    @RolesAllowed("admin")
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GET
    @Path("/{id}")
    public UserDto getUserById(@PathParam("id") UUID id) {
        validateOwnAccess(id);
        return userService.getUserById(id);
    }

    @GET
    @Path("/keycloak/{keycloakId}")
    public UserDto getUserByKeycloakId(@PathParam("keycloakId") String keycloakId) {
        if (!securityIdentity.hasRole("admin") && !keycloakId.equals(jwt.getSubject())) {
            throw new ForbiddenException("No tiene acceso a este recurso");
        }
        return userService.getUserByKeycloakId(keycloakId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public Response createUser(@Valid UserRequest request) {
        UserDto created = userService.createUser(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public UserDto updateUser(@PathParam("id") UUID id, @Valid UserRequest request) {
        return userService.updateUser(id, request);
    }

    @PATCH
    @Path("/{id}/toggle-status")
    @RolesAllowed("admin")
    public UserDto toggleUserStatus(@PathParam("id") UUID id) {
        return userService.toggleUserStatus(id);
    }

    @PUT
    @Path("/{id}/change-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "doctor", "secretary"})
    public Response changePassword(@PathParam("id") UUID id, @Valid ChangePasswordRequest request) {
        validateOwnAccess(id);
        userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        return Response.noContent().build();
    }

    private void validateOwnAccess(UUID requestedId) {
        if (securityIdentity.hasRole("admin")) return;
        String keycloakId = jwt.getSubject();
        UUID currentUserId = userRepository.findByKeycloakId(keycloakId)
                .map(UserEntity::getId)
                .orElseThrow(() -> new ForbiddenException("Usuario no encontrado"));
        if (!requestedId.equals(currentUserId)) {
            throw new ForbiddenException("No tiene acceso a este recurso");
        }
    }
}
