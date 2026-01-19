package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.dto.request.MedicalAppointmentRequest;
import gt.com.xfactory.dto.request.filter.MedicalAppointmentFilterDto;
import gt.com.xfactory.dto.request.filter.PatientFilterDto;
import gt.com.xfactory.dto.response.MedicalAppointmentDto;
import gt.com.xfactory.dto.response.MedicalHistoryPathologicalFamDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.dto.response.PatientDto;
import gt.com.xfactory.service.impl.PatientService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@RequestScoped
@Path("/api/v1/patients")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class PatientController {

    @Inject
    PatientService patientService;

    @GET
    public PageResponse<PatientDto> getPatients(
            @Valid @BeanParam PatientFilterDto filter,
            @Valid @BeanParam CommonPageRequest pageRequest) {
        return patientService.getPatients(filter, pageRequest);
    }

    @GET
    @Path("/{id}")
    public PatientDto getPatientById(@PathParam("id") UUID patientId) {
        return patientService.getPatientById(patientId);
    }

    @GET
    @Path("/{id}/medical-history-pathological-fam")
    public List<MedicalHistoryPathologicalFamDto> getMedicalHistoryPathologicalFam(@PathParam("id") UUID patientId) {
        return patientService.getMedicalHistoryPathologicalFamByPatientId(patientId);
    }

    @GET
    @Path("/{id}/appointments")
    public List<MedicalAppointmentDto> getMedicalAppointments(
            @PathParam("id") UUID patientId,
            @BeanParam MedicalAppointmentFilterDto filter) {
        return patientService.getMedicalAppointmentsByPatientId(patientId, filter);
    }

    @POST
    @Path("/{id}/appointments")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createMedicalAppointment(
            @PathParam("id") UUID patientId,
            @Valid MedicalAppointmentRequest request) {
        request.setPatientId(patientId);
        MedicalAppointmentDto created = patientService.createMedicalAppointment(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}/appointments/{appointmentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public MedicalAppointmentDto updateMedicalAppointment(
            @PathParam("id") UUID patientId,
            @PathParam("appointmentId") UUID appointmentId,
            @Valid MedicalAppointmentRequest request) {
        request.setPatientId(patientId);
        return patientService.updateMedicalAppointment(appointmentId, request);
    }

    @DELETE
    @Path("/{id}/appointments/{appointmentId}")
    public Response deleteMedicalAppointment(
            @PathParam("id") UUID patientId,
            @PathParam("appointmentId") UUID appointmentId) {
        patientService.deleteMedicalAppointment(appointmentId);
        return Response.noContent().build();
    }
}
