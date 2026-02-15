package gt.com.xfactory.dto.request.filter;

import jakarta.ws.rs.*;
import lombok.*;

@Data
public class SpecialtyFilterDto {

    @QueryParam("name")
    public String name;
}
