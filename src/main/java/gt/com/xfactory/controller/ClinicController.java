package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.service.impl.*;
import jakarta.annotation.security.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.*;


@RequestScoped
@Path("/api/v1/clinics")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "doctor", "secretary"})
public class ClinicController {
    @Inject
    ClinicService clinicService;

    @GET
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
    @RolesAllowed("admin")
    public Response createClinic(@Valid ClinicRequest request) {
        ClinicDto created = clinicService.createClinic(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public ClinicDto updateClinic(@PathParam("id") UUID id, @Valid ClinicRequest request) {
        return clinicService.updateClinic(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
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
