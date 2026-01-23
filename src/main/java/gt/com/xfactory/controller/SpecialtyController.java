package gt.com.xfactory.controller;

import gt.com.xfactory.dto.response.DoctorDto;
import gt.com.xfactory.dto.response.SpecialtyDto;
import gt.com.xfactory.service.impl.SpecialtyService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@RequestScoped
@Path("/api/v1/specialties")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin", "doctor", "secretary"})
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
