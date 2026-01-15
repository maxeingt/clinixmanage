package gt.com.xfactory.dto.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
import java.util.List;

@Data
public class CommonPageRequest {
    @QueryParam("page")
    @DefaultValue("0")
    private int page;

    @QueryParam("size")
    @DefaultValue("25")
    private int size;

    @QueryParam("sort")
    private List<String> sort;
}
