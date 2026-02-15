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
@Path("/api/v2/medical-records")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin", "doctor", "secretary"})
public class MedicalRecordV2Controller {

    @Inject
    MedicalRecordService medicalRecordService;

    @GET
    public PageResponse<MedicalRecordDto> getMedicalRecordsPaginated(
            @BeanParam MedicalRecordFilterDto filter,
            @BeanParam CommonPageRequest pageRequest) {
        return medicalRecordService.getMedicalRecordsPaginated(filter, pageRequest);
    }
}
