package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.AssignClinicPermissionRequest;
import gt.com.xfactory.dto.request.UserRequest;
import gt.com.xfactory.dto.response.UserClinicPermissionDto;
import gt.com.xfactory.dto.response.UserDto;
import gt.com.xfactory.service.impl.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@RequestScoped
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin", "doctor", "secretary"})
public class UserController {

    @Inject
    UserService userService;

    @GET
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GET
    @Path("/{id}")
    public UserDto getUserById(@PathParam("id") UUID id) {
        return userService.getUserById(id);
    }

    @GET
    @Path("/keycloak/{keycloakId}")
    public UserDto getUserByKeycloakId(@PathParam("keycloakId") String keycloakId) {
        return userService.getUserByKeycloakId(keycloakId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(@Valid UserRequest request) {
        UserDto created = userService.createUser(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public UserDto updateUser(@PathParam("id") UUID id, @Valid UserRequest request) {
        return userService.updateUser(id, request);
    }

    @PATCH
    @Path("/{id}/toggle-status")
    public UserDto toggleUserStatus(@PathParam("id") UUID id) {
        return userService.toggleUserStatus(id);
    }

    // ============ Clinic Permissions Endpoints ============

    @GET
    @Path("/{userId}/clinics")
    public List<UserClinicPermissionDto> getUserClinicPermissions(@PathParam("userId") UUID userId) {
        return userService.getUserClinicPermissions(userId);
    }

    @POST
    @Path("/{userId}/clinics")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response assignClinicPermission(
            @PathParam("userId") UUID userId,
            @Valid AssignClinicPermissionRequest request) {
        UserClinicPermissionDto permission = userService.assignClinicPermission(userId, request);
        return Response.status(Response.Status.CREATED).entity(permission).build();
    }

    @PUT
    @Path("/{userId}/clinics/{clinicId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public UserClinicPermissionDto updateClinicPermission(
            @PathParam("userId") UUID userId,
            @PathParam("clinicId") UUID clinicId,
            @Valid AssignClinicPermissionRequest request) {
        request.setClinicId(clinicId);
        return userService.assignClinicPermission(userId, request);
    }

    @DELETE
    @Path("/{userId}/clinics/{clinicId}")
    public Response revokeClinicPermission(
            @PathParam("userId") UUID userId,
            @PathParam("clinicId") UUID clinicId) {
        userService.revokeClinicPermission(userId, clinicId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{userId}/clinics/{clinicId}/access")
    public Response checkClinicAccess(
            @PathParam("userId") UUID userId,
            @PathParam("clinicId") UUID clinicId) {
        boolean hasAccess = userService.hasAccessToClinic(userId, clinicId);
        return Response.ok().entity(new AccessResponse(hasAccess)).build();
    }

    public record AccessResponse(boolean hasAccess) {}
}
