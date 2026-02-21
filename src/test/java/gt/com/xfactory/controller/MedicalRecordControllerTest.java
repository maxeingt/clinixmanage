package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.service.impl.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.*;
import io.quarkus.test.security.*;
import io.restassured.http.*;
import jakarta.ws.rs.*;
import org.junit.jupiter.api.*;

import java.time.*;
import java.util.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class MedicalRecordControllerTest {

    @InjectMock
    MedicalRecordService medicalRecordService;

    @InjectMock
    PrescriptionService prescriptionService;

    @InjectMock
    PdfService pdfService;

    // ========== GET /medical-records/patient/{patientId} ==========

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void getMedicalRecordsByPatient_asAdmin_returns200() {
        when(medicalRecordService.getMedicalRecordsByPatientId(any())).thenReturn(List.of());

        given()
            .when().get("/api/v1/medical-records/patient/" + UUID.randomUUID())
            .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void getMedicalRecordsByPatient_asDoctor_returns200() {
        when(medicalRecordService.getMedicalRecordsByPatientId(any())).thenReturn(List.of());

        given()
            .when().get("/api/v1/medical-records/patient/" + UUID.randomUUID())
            .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "secretary-user", roles = {"secretary"})
    void getMedicalRecordsByPatient_asSecretary_returns403() {
        given()
            .when().get("/api/v1/medical-records/patient/" + UUID.randomUUID())
            .then().statusCode(403);
    }

    @Test
    void getMedicalRecordsByPatient_unauthenticated_returns401() {
        given()
            .when().get("/api/v1/medical-records/patient/" + UUID.randomUUID())
            .then().statusCode(401);
    }

    // ========== GET /medical-records/appointment/{appointmentId} ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void getMedicalRecordsByAppointment_asDoctor_returns200() {
        when(medicalRecordService.getMedicalRecordsByAppointmentId(any())).thenReturn(List.of());

        given()
            .when().get("/api/v1/medical-records/appointment/" + UUID.randomUUID())
            .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void getMedicalRecordsByAppointment_differentDoctor_returns403() {
        when(medicalRecordService.getMedicalRecordsByAppointmentId(any()))
                .thenThrow(new ForbiddenException("No tiene acceso"));

        given()
            .when().get("/api/v1/medical-records/appointment/" + UUID.randomUUID())
            .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void getMedicalRecordsByAppointment_appointmentNotFound_returns404() {
        when(medicalRecordService.getMedicalRecordsByAppointmentId(any()))
                .thenThrow(new NotFoundException("Appointment not found"));

        given()
            .when().get("/api/v1/medical-records/appointment/" + UUID.randomUUID())
            .then().statusCode(404);
    }

    // ========== GET /medical-records/{id} ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void getMedicalRecordById_asDoctor_returns200() {
        MedicalRecordDto dto = medicalRecordDto();
        when(medicalRecordService.getMedicalRecordById(any())).thenReturn(dto);

        given()
            .when().get("/api/v1/medical-records/" + UUID.randomUUID())
            .then().statusCode(200)
                   .body("chiefComplaint", is("Cefalea"));
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void getMedicalRecordById_asAdmin_returns200() {
        MedicalRecordDto dto = medicalRecordDto();
        when(medicalRecordService.getMedicalRecordById(any())).thenReturn(dto);

        given()
            .when().get("/api/v1/medical-records/" + UUID.randomUUID())
            .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void getMedicalRecordById_notFound_returns404() {
        when(medicalRecordService.getMedicalRecordById(any()))
                .thenThrow(new NotFoundException("Record not found"));

        given()
            .when().get("/api/v1/medical-records/" + UUID.randomUUID())
            .then().statusCode(404);
    }

    // ========== POST /medical-records ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void createMedicalRecord_asDoctor_returns201() {
        MedicalRecordDto dto = medicalRecordDto();
        when(medicalRecordService.createMedicalRecord(any())).thenReturn(dto);

        given()
            .contentType(ContentType.JSON)
            .body(buildMedicalRecordRequestJson())
            .when().post("/api/v1/medical-records")
            .then().statusCode(201)
                   .body("chiefComplaint", is("Cefalea"));
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void createMedicalRecord_asAdmin_returns403() {
        given()
            .contentType(ContentType.JSON)
            .body(buildMedicalRecordRequestJson())
            .when().post("/api/v1/medical-records")
            .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void createMedicalRecord_missingRequiredFields_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when().post("/api/v1/medical-records")
            .then().statusCode(400);
    }

    // ========== PUT /medical-records/{id} ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void updateMedicalRecord_asDoctor_returns200() {
        MedicalRecordDto dto = medicalRecordDto();
        when(medicalRecordService.updateMedicalRecord(any(), any())).thenReturn(dto);

        given()
            .contentType(ContentType.JSON)
            .body(buildMedicalRecordRequestJson())
            .when().put("/api/v1/medical-records/" + UUID.randomUUID())
            .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void updateMedicalRecord_asAdmin_returns403() {
        given()
            .contentType(ContentType.JSON)
            .body(buildMedicalRecordRequestJson())
            .when().put("/api/v1/medical-records/" + UUID.randomUUID())
            .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void updateMedicalRecord_notFound_returns404() {
        when(medicalRecordService.updateMedicalRecord(any(), any()))
                .thenThrow(new NotFoundException("Record not found"));

        given()
            .contentType(ContentType.JSON)
            .body(buildMedicalRecordRequestJson())
            .when().put("/api/v1/medical-records/" + UUID.randomUUID())
            .then().statusCode(404);
    }

    // ========== DELETE /medical-records/{id} ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void deleteMedicalRecord_asDoctor_returns204() {
        doNothing().when(medicalRecordService).deleteMedicalRecord(any());

        given()
            .when().delete("/api/v1/medical-records/" + UUID.randomUUID())
            .then().statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void deleteMedicalRecord_asAdmin_returns403() {
        given()
            .when().delete("/api/v1/medical-records/" + UUID.randomUUID())
            .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void deleteMedicalRecord_differentDoctorRecord_returns403() {
        doThrow(new ForbiddenException("No tiene acceso"))
                .when(medicalRecordService).deleteMedicalRecord(any());

        given()
            .when().delete("/api/v1/medical-records/" + UUID.randomUUID())
            .then().statusCode(403);
    }

    // ========== GET /medical-records/prescriptions/patient/{patientId} ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void getPrescriptionsByPatient_asDoctor_returns200() {
        when(prescriptionService.getPrescriptionsByPatientId(any())).thenReturn(List.of());

        given()
            .when().get("/api/v1/medical-records/prescriptions/patient/" + UUID.randomUUID())
            .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void getPrescriptionsByPatient_asAdmin_returns200() {
        when(prescriptionService.getPrescriptionsByPatientId(any())).thenReturn(List.of());

        given()
            .when().get("/api/v1/medical-records/prescriptions/patient/" + UUID.randomUUID())
            .then().statusCode(200);
    }

    // ========== GET /medical-records/prescriptions/patient/{patientId}/active ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void getActivePrescriptionsByPatient_asDoctor_returns200() {
        when(prescriptionService.getActivePrescriptionsByPatientId(any())).thenReturn(List.of());

        given()
            .when().get("/api/v1/medical-records/prescriptions/patient/" + UUID.randomUUID() + "/active")
            .then().statusCode(200);
    }

    // ========== GET /medical-records/{recordId}/prescriptions ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void getPrescriptionsByRecord_asDoctor_returns200() {
        when(prescriptionService.getPrescriptionsByMedicalRecordId(any())).thenReturn(List.of());

        given()
            .when().get("/api/v1/medical-records/" + UUID.randomUUID() + "/prescriptions")
            .then().statusCode(200);
    }

    // ========== POST /medical-records/prescriptions ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void createPrescription_asDoctor_returns201() {
        PrescriptionDto dto = prescriptionDto();
        when(prescriptionService.createPrescription(any())).thenReturn(dto);

        given()
            .contentType(ContentType.JSON)
            .body(buildPrescriptionRequestJson())
            .when().post("/api/v1/medical-records/prescriptions")
            .then().statusCode(201);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void createPrescription_asAdmin_returns403() {
        given()
            .contentType(ContentType.JSON)
            .body(buildPrescriptionRequestJson())
            .when().post("/api/v1/medical-records/prescriptions")
            .then().statusCode(403);
    }

    // ========== DELETE /medical-records/prescriptions/{id} ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void deletePrescription_asDoctor_returns204() {
        doNothing().when(prescriptionService).deletePrescription(any());

        given()
            .when().delete("/api/v1/medical-records/prescriptions/" + UUID.randomUUID())
            .then().statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void deletePrescription_asAdmin_returns403() {
        given()
            .when().delete("/api/v1/medical-records/prescriptions/" + UUID.randomUUID())
            .then().statusCode(403);
    }

    // ========== helpers ==========

    private MedicalRecordDto medicalRecordDto() {
        return MedicalRecordDto.builder()
                .id(UUID.randomUUID())
                .patientId(UUID.randomUUID())
                .patientName("Ana Martínez")
                .doctorId(UUID.randomUUID())
                .doctorName("Dr. Luis Ramírez")
                .chiefComplaint("Cefalea")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private PrescriptionDto prescriptionDto() {
        return PrescriptionDto.builder()
                .id(UUID.randomUUID())
                .patientId(UUID.randomUUID())
                .doctorId(UUID.randomUUID())
                .build();
    }

    private String buildMedicalRecordRequestJson() {
        return """
                {
                    "patientId": "%s",
                    "doctorId": "%s",
                    "chiefComplaint": "Cefalea intensa"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());
    }

    private String buildPrescriptionRequestJson() {
        return """
                {
                    "medicalRecordId": "%s",
                    "patientId": "%s",
                    "doctorId": "%s",
                    "medications": []
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }
}
