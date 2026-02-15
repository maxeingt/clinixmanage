package gt.com.xfactory.controller.v2;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.service.impl.*;
import jakarta.annotation.security.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@RequestScoped
@Path("/api/v2/users")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
public class UserV2Controller {

    @Inject
    UserService userService;

    @GET
    public PageResponse<UserDto> getUsersPaginated(
            @BeanParam UserFilterDto filter,
            @BeanParam CommonPageRequest pageRequest) {
        return userService.getUsersPaginated(filter, pageRequest);
    }
}
