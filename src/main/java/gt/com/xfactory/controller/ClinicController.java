package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.ClinicRequest;
import gt.com.xfactory.dto.request.filter.DoctorFilterDto;
import gt.com.xfactory.dto.request.filter.MedicalAppointmentFilterDto;
import gt.com.xfactory.dto.response.ClinicDto;
import gt.com.xfactory.dto.response.DoctorDto;
import gt.com.xfactory.dto.response.MedicalAppointmentDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.service.impl.ClinicService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@RequestScoped
@Path("/api/v1/clinics")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class ClinicController {
    @Inject
    ClinicService clinicService;

    @GET
    @PermitAll
    public List<ClinicDto> getAllClinics() {
        return clinicService.getAllClinics();
    }

    @GET
    @Path("/{id}")
    public ClinicDto getClinicById(@PathParam("id") UUID id) {
        return clinicService.getClinicById(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createClinic(@Valid ClinicRequest request) {
        ClinicDto created = clinicService.createClinic(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public ClinicDto updateClinic(@PathParam("id") UUID id, @Valid ClinicRequest request) {
        return clinicService.updateClinic(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteClinic(@PathParam("id") UUID id) {
        clinicService.deleteClinic(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/doctors")
    public PageResponse<DoctorDto> getDoctorsByClinic(
            @PathParam("id") UUID clinicId,
            @Valid @BeanParam DoctorFilterDto filter,
            @BeanParam CommonPageRequest pageRequest) {
       return clinicService.getDoctorsByClinic(clinicId, filter, pageRequest);
    }

    @GET
    @Path("/{id}/appointments")
    public List<MedicalAppointmentDto> getAppointmentsByClinic(
            @PathParam("id") UUID clinicId,
            @BeanParam MedicalAppointmentFilterDto filter) {
        return clinicService.getAppointmentsByClinic(clinicId, filter);
    }
}
