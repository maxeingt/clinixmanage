package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.MedicalRecordRequest;
import gt.com.xfactory.dto.request.PrescriptionRequest;
import gt.com.xfactory.dto.response.MedicalRecordDto;
import gt.com.xfactory.dto.response.PrescriptionDto;
import gt.com.xfactory.dto.response.SpecialtyFormTemplateDto;
import gt.com.xfactory.service.impl.MedicalRecordService;
import gt.com.xfactory.service.impl.PdfService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
@Path("/api/v1/medical-records")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin", "doctor", "secretary"})
public class MedicalRecordController {

    @Inject
    MedicalRecordService medicalRecordService;

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
    public Response createMedicalRecord(@Valid MedicalRecordRequest request) {
        MedicalRecordDto created = medicalRecordService.createMedicalRecord(
                request.getPatientId(),
                request.getAppointmentId(),
                request.getDoctorId(),
                request.getSpecialtyId(),
                request.getRecordType(),
                request.getChiefComplaint(),
                request.getPresentIllness(),
                request.getPhysicalExam(),
                request.getDiagnosis(),
                request.getTreatmentPlan(),
                request.getVitalSigns(),
                request.getSpecialtyData(),
                request.getAttachments()
        );
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public MedicalRecordDto updateMedicalRecord(
            @PathParam("id") UUID recordId,
            @Valid MedicalRecordRequest request) {
        return medicalRecordService.updateMedicalRecord(
                recordId,
                request.getChiefComplaint(),
                request.getPresentIllness(),
                request.getPhysicalExam(),
                request.getDiagnosis(),
                request.getTreatmentPlan(),
                request.getVitalSigns(),
                request.getSpecialtyData(),
                request.getAttachments()
        );
    }

    @DELETE
    @Path("/{id}")
    public Response deleteMedicalRecord(@PathParam("id") UUID recordId) {
        medicalRecordService.deleteMedicalRecord(recordId);
        return Response.noContent().build();
    }

    // Specialty Form Template endpoints

    @GET
    @Path("/form-templates")
    public List<SpecialtyFormTemplateDto> getAllActiveFormTemplates() {
        return medicalRecordService.getAllActiveFormTemplates();
    }

    @GET
    @Path("/form-templates/specialty/{specialtyId}")
    public List<SpecialtyFormTemplateDto> getFormTemplatesBySpecialtyId(@PathParam("specialtyId") UUID specialtyId) {
        return medicalRecordService.getFormTemplatesBySpecialtyId(specialtyId);
    }

    @GET
    @Path("/form-templates/{id}")
    public SpecialtyFormTemplateDto getFormTemplateById(@PathParam("id") UUID templateId) {
        return medicalRecordService.getFormTemplateById(templateId);
    }

    // Prescription endpoints

    @GET
    @Path("/prescriptions/patient/{patientId}")
    public List<PrescriptionDto> getPrescriptionsByPatientId(@PathParam("patientId") UUID patientId) {
        return medicalRecordService.getPrescriptionsByPatientId(patientId);
    }

    @GET
    @Path("/prescriptions/patient/{patientId}/active")
    public List<PrescriptionDto> getActivePrescriptionsByPatientId(@PathParam("patientId") UUID patientId) {
        return medicalRecordService.getActivePrescriptionsByPatientId(patientId);
    }

    @GET
    @Path("/{recordId}/prescriptions")
    public List<PrescriptionDto> getPrescriptionsByMedicalRecordId(@PathParam("recordId") UUID medicalRecordId) {
        return medicalRecordService.getPrescriptionsByMedicalRecordId(medicalRecordId);
    }

    @POST
    @Path("/prescriptions")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createPrescription(@Valid PrescriptionRequest request) {
        PrescriptionDto created = medicalRecordService.createPrescription(
                request.getPatientId(),
                request.getMedicalRecordId(),
                request.getDoctorId(),
                request.getMedications(),
                request.getNotes(),
                request.getIssueDate(),
                request.getExpiryDate()
        );
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/prescriptions/{id}/pdf")
    @Produces("application/pdf")
    public Response getPrescriptionPdf(@PathParam("id") UUID prescriptionId) {
        PrescriptionDto prescription = medicalRecordService.getPrescriptionById(prescriptionId);
        byte[] pdf = pdfService.generatePrescriptionPdf(prescription);
        return Response.ok(pdf, "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"receta-" + prescriptionId + ".pdf\"")
                .build();
    }

    @DELETE
    @Path("/prescriptions/{id}")
    public Response deletePrescription(@PathParam("id") UUID prescriptionId) {
        medicalRecordService.deletePrescription(prescriptionId);
        return Response.noContent().build();
    }
}
