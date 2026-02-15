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
@Path("/api/v1/medical-records")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin", "doctor", "secretary"})
public class MedicalRecordController {

    @Inject
    MedicalRecordService medicalRecordService;

    @Inject
    PrescriptionService prescriptionService;

    @Inject
    PdfService pdfService;

    @GET
    @Path("/patient/{patientId}")
    public List<MedicalRecordDto> getMedicalRecordsByPatientId(@PathParam("patientId") UUID patientId) {
        return medicalRecordService.getMedicalRecordsByPatientId(patientId);
    }

    @GET
    @Path("/appointment/{appointmentId}")
    public List<MedicalRecordDto> getMedicalRecordsByAppointmentId(@PathParam("appointmentId") UUID appointmentId) {
        return medicalRecordService.getMedicalRecordsByAppointmentId(appointmentId);
    }

    @GET
    @Path("/{id}")
    public MedicalRecordDto getMedicalRecordById(@PathParam("id") UUID recordId) {
        return medicalRecordService.getMedicalRecordById(recordId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "doctor"})
    public Response createMedicalRecord(@Valid MedicalRecordRequest request) {
        MedicalRecordDto created = medicalRecordService.createMedicalRecord(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "doctor"})
    public MedicalRecordDto updateMedicalRecord(
            @PathParam("id") UUID recordId,
            @Valid MedicalRecordRequest request) {
        return medicalRecordService.updateMedicalRecord(recordId, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"admin", "doctor"})
    public Response deleteMedicalRecord(@PathParam("id") UUID recordId) {
        medicalRecordService.deleteMedicalRecord(recordId);
        return Response.noContent().build();
    }

    // Prescription endpoints

    @GET
    @Path("/prescriptions/patient/{patientId}")
    public List<PrescriptionDto> getPrescriptionsByPatientId(@PathParam("patientId") UUID patientId) {
        return prescriptionService.getPrescriptionsByPatientId(patientId);
    }

    @GET
    @Path("/prescriptions/patient/{patientId}/active")
    public List<PrescriptionDto> getActivePrescriptionsByPatientId(@PathParam("patientId") UUID patientId) {
        return prescriptionService.getActivePrescriptionsByPatientId(patientId);
    }

    @GET
    @Path("/{recordId}/prescriptions")
    public List<PrescriptionDto> getPrescriptionsByMedicalRecordId(@PathParam("recordId") UUID medicalRecordId) {
        return prescriptionService.getPrescriptionsByMedicalRecordId(medicalRecordId);
    }

    @POST
    @Path("/prescriptions")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "doctor"})
    public Response createPrescription(@Valid PrescriptionRequest request) {
        PrescriptionDto created = prescriptionService.createPrescription(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/prescriptions/{id}/pdf")
    @Produces("application/pdf")
    public Response getPrescriptionPdf(@PathParam("id") UUID prescriptionId) {
        PrescriptionDto prescription = prescriptionService.getPrescriptionById(prescriptionId);
        byte[] pdf = pdfService.generatePrescriptionPdf(prescription);
        return Response.ok(pdf, "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"receta-" + prescriptionId + ".pdf\"")
                .build();
    }

    @DELETE
    @Path("/prescriptions/{id}")
    @RolesAllowed({"admin", "doctor"})
    public Response deletePrescription(@PathParam("id") UUID prescriptionId) {
        prescriptionService.deletePrescription(prescriptionId);
        return Response.noContent().build();
    }
}
