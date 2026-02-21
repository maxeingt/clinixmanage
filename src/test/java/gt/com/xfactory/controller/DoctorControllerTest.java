package gt.com.xfactory.controller;

import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.service.impl.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.*;
import io.quarkus.test.security.*;
import io.restassured.http.*;
import jakarta.ws.rs.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class DoctorControllerTest {

    @InjectMock
    DoctorService doctorService;

    // ========== GET /doctors ==========

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void getDoctors_asAdmin_returns200() {
        when(doctorService.getDoctors(any(), any()))
                .thenReturn(new PageResponse<>(List.of(), 0, 0, 0));

        given()
            .when().get("/api/v1/doctors")
            .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "secretary-user", roles = {"secretary"})
    void getDoctors_asSecretary_returns200() {
        when(doctorService.getDoctors(any(), any()))
                .thenReturn(new PageResponse<>(List.of(), 0, 0, 0));

        given()
            .when().get("/api/v1/doctors")
            .then().statusCode(200);
    }

    // ========== POST /doctors ==========

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void createDoctor_asAdmin_returns201() {
        DoctorDto created = doctorDto();
        when(doctorService.createDoctor(any())).thenReturn(created);

        given()
            .contentType(ContentType.JSON)
            .body(buildDoctorRequestJson())
            .when().post("/api/v1/doctors")
            .then().statusCode(201)
                   .body("firstName", is("Carlos"));
    }

    @Test
    @TestSecurity(user = "secretary-user", roles = {"secretary"})
    void createDoctor_asSecretary_returns403() {
        given()
            .contentType(ContentType.JSON)
            .body(buildDoctorRequestJson())
            .when().post("/api/v1/doctors")
            .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void createDoctor_asDoctor_returns403() {
        given()
            .contentType(ContentType.JSON)
            .body(buildDoctorRequestJson())
            .when().post("/api/v1/doctors")
            .then().statusCode(403);
    }

    // ========== DELETE /doctors/{id} ==========

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void deleteDoctor_asAdmin_returns204() {
        doNothing().when(doctorService).deleteDoctor(any(UUID.class));

        given()
            .when().delete("/api/v1/doctors/" + UUID.randomUUID())
            .then().statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void deleteDoctor_withAppointments_returns409() {
        doThrow(new IllegalStateException("No se puede eliminar: tiene citas médicas"))
                .when(doctorService).deleteDoctor(any(UUID.class));

        given()
            .when().delete("/api/v1/doctors/" + UUID.randomUUID())
            .then().statusCode(409);
    }

    // ========== Clinic endpoints ==========

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void addClinicToDoctor_asAdmin_returns201() {
        UUID doctorId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();
        ClinicDto clinic = ClinicDto.builder()
                .id(clinicId).name("Clínica Test").build();
        when(doctorService.addClinicToDoctor(any(), any())).thenReturn(clinic);

        given()
            .when().post("/api/v1/doctors/" + doctorId + "/clinics/" + clinicId)
            .then().statusCode(201)
                   .body("name", is("Clínica Test"));
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void removeClinicFromDoctor_asAdmin_returns204() {
        UUID doctorId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();
        doNothing().when(doctorService).removeClinicFromDoctor(any(), any());

        given()
            .when().delete("/api/v1/doctors/" + doctorId + "/clinics/" + clinicId)
            .then().statusCode(204);
    }

    @Test
    @TestSecurity(user = "secretary-user", roles = {"secretary"})
    void addClinicToDoctor_asSecretary_returns403() {
        given()
            .when().post("/api/v1/doctors/" + UUID.randomUUID() + "/clinics/" + UUID.randomUUID())
            .then().statusCode(403);
    }

    // ========== Unauthenticated ==========

    @Test
    void getDoctors_unauthenticated_returns401() {
        given()
            .when().get("/api/v1/doctors")
            .then().statusCode(401);
    }

    // ========== helpers ==========

    private DoctorDto doctorDto() {
        return DoctorDto.builder()
                .id(UUID.randomUUID())
                .firstName("Carlos")
                .lastName("Médico")
                .build();
    }

    private String buildDoctorRequestJson() {
        return """
                {
                    "firstName": "Carlos",
                    "lastName": "Médico"
                }
                """;
    }
}
