package gt.com.xfactory.controller;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.enums.*;
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
class LabOrderControllerTest {

    @InjectMock
    LabOrderService labOrderService;

    @InjectMock
    PdfService pdfService;

    // ========== GET /lab-orders ==========

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void getLabOrders_asAdmin_returns200() {
        when(labOrderService.getLabOrders(any(), any()))
                .thenReturn(new PageResponse<>(List.of(), 0, 0, 0));

        given()
            .when().get("/api/v1/lab-orders")
            .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void getLabOrders_asDoctor_returns200() {
        when(labOrderService.getLabOrders(any(), any()))
                .thenReturn(new PageResponse<>(List.of(), 0, 0, 0));

        given()
            .when().get("/api/v1/lab-orders")
            .then().statusCode(200);
    }

    @Test
    void getLabOrders_unauthenticated_returns401() {
        given()
            .when().get("/api/v1/lab-orders")
            .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "secretary-user", roles = {"secretary"})
    void getLabOrders_asSecretary_returns403() {
        given()
            .when().get("/api/v1/lab-orders")
            .then().statusCode(403);
    }

    // ========== GET /lab-orders/{id} ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void getLabOrderById_asDoctor_returns200() {
        LabOrderDto dto = labOrderDto();
        when(labOrderService.getLabOrderById(any())).thenReturn(dto);

        given()
            .when().get("/api/v1/lab-orders/" + UUID.randomUUID())
            .then().statusCode(200)
                   .body("notes", is("Análisis rutinario"));
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void getLabOrderById_notFound_returns404() {
        when(labOrderService.getLabOrderById(any()))
                .thenThrow(new NotFoundException("Lab order not found"));

        given()
            .when().get("/api/v1/lab-orders/" + UUID.randomUUID())
            .then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void getLabOrderById_differentDoctor_returns403() {
        when(labOrderService.getLabOrderById(any()))
                .thenThrow(new ForbiddenException("No tiene acceso"));

        given()
            .when().get("/api/v1/lab-orders/" + UUID.randomUUID())
            .then().statusCode(403);
    }

    // ========== GET /lab-orders/patient/{patientId} ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void getLabOrdersByPatient_asDoctor_returns200() {
        when(labOrderService.getLabOrdersByPatientId(any())).thenReturn(List.of());

        given()
            .when().get("/api/v1/lab-orders/patient/" + UUID.randomUUID())
            .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void getLabOrdersByPatient_asAdmin_returns200() {
        when(labOrderService.getLabOrdersByPatientId(any())).thenReturn(List.of(labOrderDto()));

        given()
            .when().get("/api/v1/lab-orders/patient/" + UUID.randomUUID())
            .then().statusCode(200)
                   .body("size()", is(1));
    }

    // ========== POST /lab-orders ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void createLabOrder_asDoctor_returns201() {
        LabOrderDto dto = labOrderDto();
        when(labOrderService.createLabOrder(any())).thenReturn(dto);

        given()
            .contentType(ContentType.JSON)
            .body(buildLabOrderRequestJson())
            .when().post("/api/v1/lab-orders")
            .then().statusCode(201);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void createLabOrder_asAdmin_returns403() {
        given()
            .contentType(ContentType.JSON)
            .body(buildLabOrderRequestJson())
            .when().post("/api/v1/lab-orders")
            .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void createLabOrder_missingRequiredFields_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when().post("/api/v1/lab-orders")
            .then().statusCode(400);
    }

    // ========== PUT /lab-orders/{id} ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void updateLabOrder_asDoctor_returns200() {
        LabOrderDto dto = labOrderDto();
        when(labOrderService.updateLabOrder(any(), any())).thenReturn(dto);

        given()
            .contentType(ContentType.JSON)
            .body(buildLabOrderRequestJson())
            .when().put("/api/v1/lab-orders/" + UUID.randomUUID())
            .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void updateLabOrder_asAdmin_returns403() {
        given()
            .contentType(ContentType.JSON)
            .body(buildLabOrderRequestJson())
            .when().put("/api/v1/lab-orders/" + UUID.randomUUID())
            .then().statusCode(403);
    }

    // ========== PATCH /lab-orders/{id}/status ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void updateStatus_asDoctor_returns200() {
        LabOrderDto dto = labOrderDto();
        dto.setStatus(LabOrderStatus.in_progress);
        when(labOrderService.updateStatus(any(), any())).thenReturn(dto);

        given()
            .contentType(ContentType.JSON)
            .body("{\"status\": \"in_progress\"}")
            .when().patch("/api/v1/lab-orders/" + UUID.randomUUID() + "/status")
            .then().statusCode(200)
                   .body("status", is("in_progress"));
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void updateStatus_asAdmin_returns403() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"status\": \"in_progress\"}")
            .when().patch("/api/v1/lab-orders/" + UUID.randomUUID() + "/status")
            .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void updateStatus_missingStatus_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when().patch("/api/v1/lab-orders/" + UUID.randomUUID() + "/status")
            .then().statusCode(400);
    }

    // ========== DELETE /lab-orders/{id} ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void deleteLabOrder_asDoctor_returns204() {
        doNothing().when(labOrderService).deleteLabOrder(any());

        given()
            .when().delete("/api/v1/lab-orders/" + UUID.randomUUID())
            .then().statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void deleteLabOrder_asAdmin_returns403() {
        given()
            .when().delete("/api/v1/lab-orders/" + UUID.randomUUID())
            .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void deleteLabOrder_notFound_returns404() {
        doThrow(new NotFoundException("Lab order not found"))
                .when(labOrderService).deleteLabOrder(any());

        given()
            .when().delete("/api/v1/lab-orders/" + UUID.randomUUID())
            .then().statusCode(404);
    }

    // ========== POST /lab-orders/{orderId}/results ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void addResult_asDoctor_returns201() {
        LabResultDto dto = LabResultDto.builder()
                .id(UUID.randomUUID())
                .testName("Glucosa")
                .isAbnormal(false)
                .build();
        when(labOrderService.addResult(any(), any())).thenReturn(dto);

        given()
            .contentType(ContentType.JSON)
            .body("{\"testName\": \"Glucosa\"}")
            .when().post("/api/v1/lab-orders/" + UUID.randomUUID() + "/results")
            .then().statusCode(201)
                   .body("testName", is("Glucosa"));
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void addResult_asAdmin_returns403() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"testName\": \"Glucosa\"}")
            .when().post("/api/v1/lab-orders/" + UUID.randomUUID() + "/results")
            .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void addResult_missingTestName_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when().post("/api/v1/lab-orders/" + UUID.randomUUID() + "/results")
            .then().statusCode(400);
    }

    // ========== PUT /lab-orders/results/{resultId} ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void updateResult_asDoctor_returns200() {
        LabResultDto dto = LabResultDto.builder()
                .id(UUID.randomUUID())
                .testName("Hemograma completo")
                .isAbnormal(true)
                .build();
        when(labOrderService.updateResult(any(), any())).thenReturn(dto);

        given()
            .contentType(ContentType.JSON)
            .body("{\"testName\": \"Hemograma completo\", \"isAbnormal\": true}")
            .when().put("/api/v1/lab-orders/results/" + UUID.randomUUID())
            .then().statusCode(200)
                   .body("testName", is("Hemograma completo"));
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void updateResult_asAdmin_returns403() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"testName\": \"Test\"}")
            .when().put("/api/v1/lab-orders/results/" + UUID.randomUUID())
            .then().statusCode(403);
    }

    // ========== DELETE /lab-orders/results/{resultId} ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void deleteResult_asDoctor_returns204() {
        doNothing().when(labOrderService).deleteResult(any());

        given()
            .when().delete("/api/v1/lab-orders/results/" + UUID.randomUUID())
            .then().statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void deleteResult_asAdmin_returns403() {
        given()
            .when().delete("/api/v1/lab-orders/results/" + UUID.randomUUID())
            .then().statusCode(403);
    }

    // ========== DELETE /lab-orders/attachments/{attachmentId} ==========

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void deleteAttachment_asDoctor_returns204() {
        doNothing().when(labOrderService).deleteAttachment(any());

        given()
            .when().delete("/api/v1/lab-orders/attachments/" + UUID.randomUUID())
            .then().statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void deleteAttachment_asAdmin_returns403() {
        given()
            .when().delete("/api/v1/lab-orders/attachments/" + UUID.randomUUID())
            .then().statusCode(403);
    }

    // ========== helpers ==========

    private LabOrderDto labOrderDto() {
        return LabOrderDto.builder()
                .id(UUID.randomUUID())
                .patientId(UUID.randomUUID())
                .patientName("Pedro Sánchez")
                .doctorId(UUID.randomUUID())
                .doctorName("Dra. Elena Vásquez")
                .status(LabOrderStatus.pending)
                .notes("Análisis rutinario")
                .orderDate(LocalDateTime.now())
                .results(List.of())
                .attachments(List.of())
                .build();
    }

    private String buildLabOrderRequestJson() {
        return """
                {
                    "patientId": "%s",
                    "doctorId": "%s",
                    "notes": "Análisis de rutina"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());
    }
}
