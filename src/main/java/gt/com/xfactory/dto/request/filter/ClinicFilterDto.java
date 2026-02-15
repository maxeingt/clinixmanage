package gt.com.xfactory.dto.request.filter;

import jakarta.ws.rs.*;
import lombok.*;

@Data
public class ClinicFilterDto {

    @QueryParam("name")
    public String name;

    @QueryParam("address")
    public String address;

    @QueryParam("phone")
    public String phone;
}
