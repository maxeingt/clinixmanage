package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.DoctorRequest;
import gt.com.xfactory.dto.request.filter.DoctorFilterDto;
import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.dto.response.ClinicDto;
import gt.com.xfactory.dto.response.DoctorDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.dto.response.SpecialtyDto;
import gt.com.xfactory.service.impl.DoctorService;
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
@Path("/api/v1/doctors")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin", "doctor", "secretary"})
public class DoctorController {
    @Inject
    DoctorService doctorService;

    @GET
    public PageResponse<DoctorDto> getDoctors(
            @Valid @BeanParam DoctorFilterDto filter,
            @Valid @BeanParam CommonPageRequest pageRequest) {
        return doctorService.getDoctors(filter, pageRequest);
    }

    @GET
    @Path("/by-user/{userId}")
    public DoctorDto getDoctorByUserId(@PathParam("userId") UUID userId) {
        return doctorService.getDoctorByUserId(userId);
    }

    @GET
    @Path("/{id}")
    public DoctorDto getDoctorById(@PathParam("id") UUID id) {
        return doctorService.getDoctorById(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public Response createDoctor(@Valid DoctorRequest request) {
        DoctorDto created = doctorService.createDoctor(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public DoctorDto updateDoctor(@PathParam("id") UUID id, @Valid DoctorRequest request) {
        return doctorService.updateDoctor(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    public Response deleteDoctor(@PathParam("id") UUID id) {
        doctorService.deleteDoctor(id);
        return Response.noContent().build();
    }

    // ============ Specialty Endpoints ============

    @GET
    @Path("/{doctorId}/specialties")
    public List<SpecialtyDto> getDoctorSpecialties(@PathParam("doctorId") UUID doctorId) {
        return doctorService.getDoctorSpecialties(doctorId);
    }

    @POST
    @Path("/{doctorId}/specialties/{specialtyId}")
    @RolesAllowed("admin")
    public Response addSpecialtyToDoctor(
            @PathParam("doctorId") UUID doctorId,
            @PathParam("specialtyId") UUID specialtyId) {
        SpecialtyDto specialty = doctorService.addSpecialtyToDoctor(doctorId, specialtyId);
        return Response.status(Response.Status.CREATED).entity(specialty).build();
    }

    @DELETE
    @Path("/{doctorId}/specialties/{specialtyId}")
    @RolesAllowed("admin")
    public Response removeSpecialtyFromDoctor(
            @PathParam("doctorId") UUID doctorId,
            @PathParam("specialtyId") UUID specialtyId) {
        doctorService.removeSpecialtyFromDoctor(doctorId, specialtyId);
        return Response.noContent().build();
    }

    // ============ Clinic Endpoints ============

    @GET
    @Path("/{doctorId}/clinics")
    public List<ClinicDto> getDoctorClinics(@PathParam("doctorId") UUID doctorId) {
        return doctorService.getDoctorClinics(doctorId);
    }

    @POST
    @Path("/{doctorId}/clinics/{clinicId}")
    @RolesAllowed("admin")
    public Response addClinicToDoctor(
            @PathParam("doctorId") UUID doctorId,
            @PathParam("clinicId") UUID clinicId) {
        ClinicDto clinic = doctorService.addClinicToDoctor(doctorId, clinicId);
        return Response.status(Response.Status.CREATED).entity(clinic).build();
    }

    @DELETE
    @Path("/{doctorId}/clinics/{clinicId}")
    @RolesAllowed("admin")
    public Response removeClinicFromDoctor(
            @PathParam("doctorId") UUID doctorId,
            @PathParam("clinicId") UUID clinicId) {
        doctorService.removeClinicFromDoctor(doctorId, clinicId);
        return Response.noContent().build();
    }
}
