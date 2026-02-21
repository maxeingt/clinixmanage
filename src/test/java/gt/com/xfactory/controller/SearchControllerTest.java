package gt.com.xfactory.controller;

import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.service.impl.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.*;
import io.quarkus.test.security.*;
import jakarta.ws.rs.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class SearchControllerTest {

    @InjectMock
    SearchService searchService;

    // ========== GET /search ==========

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void search_validQuery_returns200() {
        GlobalSearchDto result = GlobalSearchDto.builder()
                .patients(List.of())
                .doctors(List.of())
                .build();
        when(searchService.search(eq("marisol"), any())).thenReturn(result);

        given()
            .queryParam("q", "marisol")
            .queryParam("types", "patients")
            .when().get("/api/v1/search")
            .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void search_shortQuery_returns400() {
        when(searchService.search(eq("ma"), any()))
                .thenThrow(new BadRequestException("El término de búsqueda debe tener al menos 3 caracteres"));

        given()
            .queryParam("q", "ma")
            .when().get("/api/v1/search")
            .then().statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void search_nullQuery_returns400() {
        when(searchService.search(isNull(), any()))
                .thenThrow(new BadRequestException("El término de búsqueda debe tener al menos 3 caracteres"));

        given()
            .when().get("/api/v1/search")
            .then().statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void search_validQueryNoTypes_returns200WithAllCategories() {
        GlobalSearchDto result = GlobalSearchDto.builder()
                .patients(List.of())
                .appointments(List.of())
                .doctors(List.of())
                .clinics(List.of())
                .records(List.of())
                .medications(List.of())
                .build();
        when(searchService.search(anyString(), any())).thenReturn(result);

        given()
            .queryParam("q", "test")
            .when().get("/api/v1/search")
            .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "doctor-user", roles = {"doctor"})
    void search_asDoctor_returns200() {
        when(searchService.search(anyString(), any()))
                .thenReturn(GlobalSearchDto.builder().patients(List.of()).build());

        given()
            .queryParam("q", "mario")
            .queryParam("types", "patients")
            .when().get("/api/v1/search")
            .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"admin"})
    void search_withTypeDoctors_returns200() {
        GlobalSearchDto result = GlobalSearchDto.builder()
                .doctors(List.of())
                .build();
        when(searchService.search(anyString(), any())).thenReturn(result);

        given()
            .queryParam("q", "carlos")
            .queryParam("types", "doctors")
            .when().get("/api/v1/search")
            .then().statusCode(200);
    }

    @Test
    void search_unauthenticated_returns401() {
        given()
            .queryParam("q", "test")
            .when().get("/api/v1/search")
            .then().statusCode(401);
    }
}
