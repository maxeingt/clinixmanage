package gt.com.xfactory.controller;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Set;

@RequestScoped
@Path("/api/v1/admin")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
public class AdminController {

    @Inject
    JsonWebToken jwt;

    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @Path("/user-info")
    public UserInfo getUserInfo() {
        return new UserInfo(
                jwt.getName(),
                jwt.getClaim("email"),
                securityIdentity.getRoles()
        );
    }

    public record UserInfo(String username, String email, Set<String> roles) {}
}
