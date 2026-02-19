package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.service.impl.*;
import jakarta.annotation.security.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.*;

@RequestScoped
@Path("/api/v1/specialties")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "doctor", "secretary"})
public class SpecialtyController {

    @Inject
    SpecialtyService specialtyService;

    @GET
    public List<SpecialtyDto> getAllSpecialties() {
        return specialtyService.getAllSpecialties();
    }

    @GET
    @Path("/{id}")
    public SpecialtyDto getSpecialtyById(@PathParam("id") UUID specialtyId) {
        return specialtyService.getSpecialtyById(specialtyId);
    }

    @GET
    @Path("/{id}/doctors")
    public List<DoctorDto> getDoctorsBySpecialty(@PathParam("id") UUID specialtyId) {
        return specialtyService.getDoctorsBySpecialtyId(specialtyId);
    }
}
