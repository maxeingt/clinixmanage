package gt.com.xfactory.service;

import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.repository.*;
import gt.com.xfactory.service.impl.*;
import io.quarkus.hibernate.orm.panache.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;
import org.junit.jupiter.api.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class SearchServiceTest {

    @InjectMock
    PatientRepository patientRepository;

    @InjectMock
    MedicalAppointmentRepository medicalAppointmentRepository;

    @InjectMock
    DoctorRepository doctorRepository;

    @InjectMock
    ClinicRepository clinicRepository;

    @InjectMock
    MedicalRecordRepository medicalRecordRepository;

    @InjectMock
    MedicationRepository medicationRepository;

    @InjectMock
    DoctorSpecialtyRepository doctorSpecialtyRepository;

    @InjectMock
    SecurityContextService securityContextService;

    @Inject
    SearchService searchService;

    // ========== Validación de longitud del término ==========

    @Test
    void search_withNullTerm_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> searchService.search(null, null));
    }

    @Test
    void search_withOneCharTerm_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> searchService.search("a", null));
    }

    @Test
    void search_withTwoCharTerm_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> searchService.search("ab", null));
    }

    @Test
    void search_withBlankTerm_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> searchService.search("  ", null));
    }

    // ========== types=patients ==========

    @Test
    @SuppressWarnings("unchecked")
    void search_withTypesPatients_queriesOnlyPatients() {
        when(securityContextService.getCurrentDoctorId()).thenReturn(null);

        PanacheQuery<PatientEntity> mockQuery = mock(PanacheQuery.class);
        when(patientRepository.find(anyString(), any(Map.class))).thenReturn(mockQuery);
        when(mockQuery.page(anyInt(), anyInt())).thenReturn(mockQuery);
        when(mockQuery.stream()).thenReturn(Stream.empty());

        GlobalSearchDto result = searchService.search("mar", List.of("patients"));

        assertNotNull(result.getPatients());
        assertNull(result.getDoctors(), "No debe incluir doctors cuando type=patients");
        assertNull(result.getAppointments(), "No debe incluir appointments cuando type=patients");
        verify(patientRepository).find(anyString(), any(Map.class));
        verify(doctorRepository, never()).find(anyString(), any(Map.class));
    }

    // ========== types=doctors ==========

    @Test
    @SuppressWarnings("unchecked")
    void search_withTypesDoctors_queriesOnlyDoctors() {
        when(securityContextService.getCurrentDoctorId()).thenReturn(null);

        PanacheQuery<DoctorEntity> mockDoctorQuery = mock(PanacheQuery.class);
        when(doctorRepository.find(anyString(), any(Map.class))).thenReturn(mockDoctorQuery);
        when(mockDoctorQuery.page(anyInt(), anyInt())).thenReturn(mockDoctorQuery);
        when(mockDoctorQuery.list()).thenReturn(List.of());
        when(doctorSpecialtyRepository.findByDoctorIds(anyList())).thenReturn(Map.of());

        GlobalSearchDto result = searchService.search("car", List.of("doctors"));

        assertNotNull(result.getDoctors());
        assertNull(result.getPatients(), "No debe incluir patients cuando type=doctors");
        verify(doctorRepository).find(anyString(), any(Map.class));
        verify(patientRepository, never()).find(anyString(), any(Map.class));
    }

    // ========== types=null → todos ==========

    @Test
    @SuppressWarnings("unchecked")
    void search_withNoTypes_queriesAllTypes() {
        when(securityContextService.getCurrentDoctorId()).thenReturn(null);

        // Stub patient query
        PanacheQuery<PatientEntity> mockPatientQuery = mock(PanacheQuery.class);
        when(patientRepository.find(anyString(), any(Map.class))).thenReturn(mockPatientQuery);
        when(mockPatientQuery.page(anyInt(), anyInt())).thenReturn(mockPatientQuery);
        when(mockPatientQuery.stream()).thenReturn(Stream.empty());

        // Stub appointment query
        PanacheQuery<MedicalAppointmentEntity> mockApptQuery = mock(PanacheQuery.class);
        when(medicalAppointmentRepository.find(anyString(), any(Map.class))).thenReturn(mockApptQuery);
        when(mockApptQuery.page(anyInt(), anyInt())).thenReturn(mockApptQuery);
        when(mockApptQuery.stream()).thenReturn(Stream.empty());

        // Stub doctor query
        PanacheQuery<DoctorEntity> mockDoctorQuery = mock(PanacheQuery.class);
        when(doctorRepository.find(anyString(), any(Map.class))).thenReturn(mockDoctorQuery);
        when(mockDoctorQuery.page(anyInt(), anyInt())).thenReturn(mockDoctorQuery);
        when(mockDoctorQuery.list()).thenReturn(List.of());
        when(doctorSpecialtyRepository.findByDoctorIds(anyList())).thenReturn(Map.of());

        // Stub clinic query
        PanacheQuery<ClinicEntity> mockClinicQuery = mock(PanacheQuery.class);
        when(clinicRepository.find(anyString(), any(Map.class))).thenReturn(mockClinicQuery);
        when(mockClinicQuery.page(anyInt(), anyInt())).thenReturn(mockClinicQuery);
        when(mockClinicQuery.stream()).thenReturn(Stream.empty());

        // Stub record query
        PanacheQuery<MedicalRecordEntity> mockRecordQuery = mock(PanacheQuery.class);
        when(medicalRecordRepository.find(anyString(), any(Map.class))).thenReturn(mockRecordQuery);
        when(mockRecordQuery.page(anyInt(), anyInt())).thenReturn(mockRecordQuery);
        when(mockRecordQuery.stream()).thenReturn(Stream.empty());

        // Stub medication query
        PanacheQuery<MedicationEntity> mockMedQuery = mock(PanacheQuery.class);
        when(medicationRepository.find(anyString(), any(Map.class))).thenReturn(mockMedQuery);
        when(mockMedQuery.page(anyInt(), anyInt())).thenReturn(mockMedQuery);
        when(mockMedQuery.stream()).thenReturn(Stream.empty());

        GlobalSearchDto result = searchService.search("test", null);

        assertNotNull(result.getPatients());
        assertNotNull(result.getAppointments());
        assertNotNull(result.getDoctors());
        assertNotNull(result.getClinics());
        assertNotNull(result.getRecords());
        assertNotNull(result.getMedications());
    }

    // ========== currentDoctorId filtra appointments y records ==========

    @Test
    @SuppressWarnings("unchecked")
    void search_withDoctorRole_passesCurrentDoctorIdToAppointmentQuery() {
        UUID doctorId = UUID.randomUUID();
        when(securityContextService.getCurrentDoctorId()).thenReturn(doctorId);

        // Solo pedimos appointments para verificar el filtro de doctor
        PanacheQuery<MedicalAppointmentEntity> mockApptQuery = mock(PanacheQuery.class);
        when(medicalAppointmentRepository.find(anyString(), any(Map.class))).thenReturn(mockApptQuery);
        when(mockApptQuery.page(anyInt(), anyInt())).thenReturn(mockApptQuery);
        when(mockApptQuery.stream()).thenReturn(Stream.empty());

        searchService.search("abc", List.of("appointments"));

        // Verificar que se llamó a find con un query que incluye filtro de doctor
        verify(medicalAppointmentRepository).find(
                argThat(query -> query.contains("doctor.id = :doctorId")),
                any(Map.class)
        );
    }
}
