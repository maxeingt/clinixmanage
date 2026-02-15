package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
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
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "doctor", "secretary"})
public class UserController {

    @Inject
    UserService userService;

    @Inject
    SecurityContextService securityContext;

    @GET
    @RolesAllowed("admin")
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GET
    @Path("/{id}")
    public UserDto getUserById(@PathParam("id") UUID id) {
        securityContext.validateOwnAccess(id);
        return userService.getUserById(id);
    }

    @GET
    @Path("/keycloak/{keycloakId}")
    public UserDto getUserByKeycloakId(@PathParam("keycloakId") String keycloakId) {
        if (!securityContext.hasRole("admin") && !keycloakId.equals(securityContext.getSubject())) {
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
        securityContext.validateOwnAccess(id);
        userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        return Response.noContent().build();
    }
}
