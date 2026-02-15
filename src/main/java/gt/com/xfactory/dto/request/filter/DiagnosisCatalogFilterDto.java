package gt.com.xfactory.dto.request.filter;

import jakarta.ws.rs.*;
import lombok.*;

@Data
public class DiagnosisCatalogFilterDto {

    @QueryParam("search")
    public String search;

    @QueryParam("code")
    public String code;
}
