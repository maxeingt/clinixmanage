package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.filter.DoctorFilterDto;
import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.dto.response.DoctorDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.service.impl.DoctorService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Path("/api/v1/doctors")
@Produces(MediaType.APPLICATION_JSON)
public class DoctorController {
    @Inject
    DoctorService doctorService;

    @GET
    public PageResponse<DoctorDto> getDoctors(
            @Valid @BeanParam DoctorFilterDto filter,
            @Valid @BeanParam CommonPageRequest pageRequest) {
        return doctorService.getDoctors(filter, pageRequest);
    }
}
