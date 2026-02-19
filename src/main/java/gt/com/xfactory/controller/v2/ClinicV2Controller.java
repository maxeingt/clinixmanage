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
@Path("/api/v2/clinics")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "doctor", "secretary"})
public class ClinicV2Controller {

    @Inject
    ClinicService clinicService;

    @GET
    public PageResponse<ClinicDto> getClinicsPaginated(
            @BeanParam ClinicFilterDto filter,
            @BeanParam CommonPageRequest pageRequest) {
        return clinicService.getClinicsPaginated(filter, pageRequest);
    }
}
