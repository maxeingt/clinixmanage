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

import java.util.List;
import java.util.UUID;

@RequestScoped
@Path("/api/v1/patients")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin", "doctor", "secretary"})
public class PatientController {

    @Inject
    PatientService patientService;

    @Inject
    MedicalAppointmentService medicalAppointmentService;

    @GET
    public PageResponse<PatientDto> getPatients(
            @Valid @BeanParam PatientFilterDto filter,
            @Valid @BeanParam CommonPageRequest pageRequest) {
        return patientService.getPatients(filter, pageRequest);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createPatient(@Valid PatientRequest request) {
        PatientDto created = patientService.createPatient(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/{id}")
    public PatientDto getPatientById(@PathParam("id") UUID patientId) {
        return patientService.getPatientById(patientId);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public PatientDto updatePatient(@PathParam("id") UUID patientId, @Valid PatientRequest request) {
        return patientService.updatePatient(patientId, request);
    }

    @DELETE
    @Path("/{id}")
    public Response deletePatient(@PathParam("id") UUID patientId) {
        patientService.deletePatient(patientId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/medical-history-pathological-fam")
    public List<MedicalHistoryPathologicalFamDto> getMedicalHistoryPathologicalFam(@PathParam("id") UUID patientId) {
        return patientService.getMedicalHistoryPathologicalFamByPatientId(patientId);
    }

    @POST
    @Path("/{id}/medical-history-pathological-fam")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createMedicalHistoryPathologicalFam(
            @PathParam("id") UUID patientId,
            @Valid MedicalHistoryPathologicalFamRequest request) {
        request.setPatientId(patientId);
        MedicalHistoryPathologicalFamDto created = patientService.createMedicalHistoryPathologicalFam(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}/medical-history-pathological-fam/{historyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public MedicalHistoryPathologicalFamDto updateMedicalHistoryPathologicalFam(
            @PathParam("id") UUID patientId,
            @PathParam("historyId") UUID historyId,
            @Valid MedicalHistoryPathologicalFamRequest request) {
        request.setPatientId(patientId);
        return patientService.updateMedicalHistoryPathologicalFam(historyId, request);
    }

    @GET
    @Path("/{id}/appointments")
    public List<MedicalAppointmentDto> getMedicalAppointments(
            @PathParam("id") UUID patientId,
            @BeanParam MedicalAppointmentFilterDto filter) {
        return medicalAppointmentService.getMedicalAppointmentsByPatientId(patientId, filter);
    }

    @POST
    @Path("/{id}/appointments")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createMedicalAppointment(
            @PathParam("id") UUID patientId,
            @Valid MedicalAppointmentRequest request) {
        request.setPatientId(patientId);
        MedicalAppointmentDto created = medicalAppointmentService.createMedicalAppointment(request);
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
        return medicalAppointmentService.updateMedicalAppointment(appointmentId, request);
    }

    @PUT
    @Path("/{id}/appointments/{appointmentId}/reopen")
    @Consumes(MediaType.APPLICATION_JSON)
    public MedicalAppointmentDto reopenMedicalAppointment(
            @PathParam("id") UUID patientId,
            @PathParam("appointmentId") UUID appointmentId,
            @Valid ReopenAppointmentRequest request) {
        return medicalAppointmentService.reopenMedicalAppointment(appointmentId, request);
    }

    @DELETE
    @Path("/{id}/appointments/{appointmentId}")
    public Response deleteMedicalAppointment(
            @PathParam("id") UUID patientId,
            @PathParam("appointmentId") UUID appointmentId) {
        medicalAppointmentService.deleteMedicalAppointment(appointmentId);
        return Response.noContent().build();
    }
}
