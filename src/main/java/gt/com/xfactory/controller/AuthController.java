package gt.com.xfactory.controller;

import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.service.impl.*;
import jakarta.annotation.security.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@RequestScoped
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"super_admin", "admin", "doctor", "secretary"})
public class AuthController {

    @Inject
    AuthService authService;

    @GET
    @Path("/me")
    public UserDto me() {
        return authService.syncCurrentUser();
    }
}
