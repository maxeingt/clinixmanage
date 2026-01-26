package gt.com.xfactory.dto.request.filter;

import jakarta.ws.rs.QueryParam;
import lombok.Data;

@Data
public class DistributorFilterDto {

    @QueryParam("name")
    public String name;

    @QueryParam("active")
    public Boolean active;
}
