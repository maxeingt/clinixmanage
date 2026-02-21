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
class PatientControllerTest {

    @InjectMock
    PatientService patientService;

    @InjectMock
    MedicalAppointmentService medicalAppointmentService;

    // ========== GET /patients ==========

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void getPatients_asAdmin_returns200() {
        when(patientService.getPatients(any(), any()))
                .thenReturn(new PageResponse<>(List.of(), 0, 0, 0));

        given()
            .when().get("/api/v1/patients")
            .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void getPatients_asDoctor_returns200() {
        when(patientService.getPatients(any(), any()))
                .thenReturn(new PageResponse<>(List.of(), 0, 0, 0));

        given()
            .when().get("/api/v1/patients")
            .then().statusCode(200);
    }

    // ========== GET /patients/search ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void searchPatients_validQuery_returns200WithList() {
        PatientSearchDto dto = PatientSearchDto.builder()
                .id(UUID.randomUUID()).firstName("Mario").lastName("Lopez")
                .birthdate(LocalDate.of(1990, 1, 1)).age(35).build();
        when(patientService.searchPatients("mar")).thenReturn(List.of(dto));

        given()
            .queryParam("q", "mar")
            .when().get("/api/v1/patients/search")
            .then().statusCode(200)
                   .body("size()", is(1));
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void searchPatients_tooShortQuery_returns400() {
        when(patientService.searchPatients("m"))
                .thenThrow(new BadRequestException("El término de búsqueda debe tener al menos 2 caracteres"));

        given()
            .queryParam("q", "m")
            .when().get("/api/v1/patients/search")
            .then().statusCode(400);
    }

    // ========== POST /patients ==========

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void createPatient_success_returns201() {
        PatientDto created = patientDto();
        when(patientService.createPatient(any())).thenReturn(created);

        given()
            .contentType(ContentType.JSON)
            .body(buildPatientRequestJson())
            .when().post("/api/v1/patients")
            .then().statusCode(201)
                   .body("firstName", is("Juan"));
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void createPatient_dpiConflict_returns409() {
        when(patientService.createPatient(any()))
                .thenThrow(new WebApplicationException(
                        jakarta.ws.rs.core.Response.status(409)
                                .entity(Map.of("message", "DPI duplicado", "existingPatientId", UUID.randomUUID().toString()))
                                .build()));

        given()
            .contentType(ContentType.JSON)
            .body(buildPatientRequestJson())
            .when().post("/api/v1/patients")
            .then().statusCode(409);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void createPatient_invalidBody_returns400() {
        // Cuerpo vacío → falla validación Bean Validation
        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when().post("/api/v1/patients")
            .then().statusCode(400);
    }

    // ========== DELETE /patients/{id} ==========

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void deletePatient_success_returns204() {
        doNothing().when(patientService).deletePatient(any(UUID.class));

        given()
            .when().delete("/api/v1/patients/" + UUID.randomUUID())
            .then().statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void deletePatient_hasRelatedData_returns409() {
        doThrow(new IllegalStateException("No se puede eliminar: tiene citas médicas"))
                .when(patientService).deletePatient(any(UUID.class));

        given()
            .when().delete("/api/v1/patients/" + UUID.randomUUID())
            .then().statusCode(409);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void deletePatient_notFound_returns404() {
        doThrow(new NotFoundException("Patient not found"))
                .when(patientService).deletePatient(any(UUID.class));

        given()
            .when().delete("/api/v1/patients/" + UUID.randomUUID())
            .then().statusCode(404);
    }

    // ========== GET /patients/{id} ==========

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void getPatientById_found_returns200() {
        PatientDto dto = patientDto();
        when(patientService.getPatientById(any())).thenReturn(dto);

        given()
            .when().get("/api/v1/patients/" + UUID.randomUUID())
            .then().statusCode(200)
                   .body("firstName", is("Juan"));
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void getPatientById_notFound_returns404() {
        when(patientService.getPatientById(any()))
                .thenThrow(new NotFoundException("Patient not found"));

        given()
            .when().get("/api/v1/patients/" + UUID.randomUUID())
            .then().statusCode(404);
    }

    // ========== Role-based access ==========

    @Test
    void getPatients_unauthenticated_returns401() {
        given()
            .when().get("/api/v1/patients")
            .then().statusCode(401);
    }

    // ========== helpers ==========

    private PatientDto patientDto() {
        return PatientDto.builder()
                .id(UUID.randomUUID())
                .firstName("Juan")
                .lastName("Perez")
                .birthdate(LocalDate.of(1990, 6, 15))
                .age(35)
                .maritalStatus("Soltero")
                .hasPathologicalHistory(false)
                .build();
    }

    private String buildPatientRequestJson() {
        return """
                {
                    "firstName": "Juan",
                    "lastName": "Perez",
                    "birthdate": "1990-06-15"
                }
                """;
    }
}
