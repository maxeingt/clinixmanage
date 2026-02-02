package gt.com.xfactory.dto.request.filter;

import jakarta.ws.rs.QueryParam;

public class DiagnosisCatalogFilterDto {

    @QueryParam("search")
    public String search;
}
