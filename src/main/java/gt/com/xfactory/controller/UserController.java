package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.UserRequest;
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
@RolesAllowed("admin")
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
}
