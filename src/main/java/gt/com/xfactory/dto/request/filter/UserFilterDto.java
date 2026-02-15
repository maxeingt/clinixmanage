package gt.com.xfactory.dto.request.filter;

import jakarta.ws.rs.*;
import lombok.*;

@Data
public class UserFilterDto {

    @QueryParam("username")
    public String username;

    @QueryParam("email")
    public String email;

    @QueryParam("role")
    public String role;
}
