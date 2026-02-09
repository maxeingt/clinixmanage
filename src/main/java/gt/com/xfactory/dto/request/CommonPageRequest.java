package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import lombok.*;
import java.util.*;

@Data
public class CommonPageRequest {
    @QueryParam("page")
    @DefaultValue("0")
    @Min(0)
    private int page;

    @QueryParam("size")
    @DefaultValue("25")
    @Min(1)
    @Max(100)
    private int size;

    @QueryParam("sort")
    private List<String> sort;
}
