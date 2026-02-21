package gt.com.xfactory.service;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.repository.*;
import gt.com.xfactory.service.impl.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class PatientServiceTest {

    @InjectMock
    PatientRepository patientRepository;

    @InjectMock
    MedicalHistoryPathologicalFamRepository medicalHistoryPathologicalFamRepository;

    @InjectMock
    MedicalAppointmentRepository medicalAppointmentRepository;

    @InjectMock
    MedicalRecordRepository medicalRecordRepository;

    @InjectMock
    PrescriptionRepository prescriptionRepository;

    @InjectMock
    SecurityContextService securityContextService;

    @Inject
    PatientService patientService;

    // ========== createPatient ==========

    @Test
    void createPatient_success_returnsDto() {
        PatientRequest request = buildPatientRequest();
        when(patientRepository.findByDpi(anyString())).thenReturn(Optional.empty());
        when(patientRepository.findByNameAndBirthdate(anyString(), anyString(), any())).thenReturn(List.of());

        PatientDto result = patientService.createPatient(request);

        assertNotNull(result);
        assertEquals("Juan", result.getFirstName());
        assertEquals("Perez", result.getLastName());
        verify(patientRepository).persist(any(PatientEntity.class));
    }

    @Test
    void createPatient_duplicateDpi_throwsConflict() {
        PatientRequest request = buildPatientRequest();
        PatientEntity existing = patientEntityWith(UUID.randomUUID());
        when(patientRepository.findByDpi(anyString())).thenReturn(Optional.of(existing));

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> patientService.createPatient(request));

        assertEquals(409, ex.getResponse().getStatus());
    }

    @Test
    void createPatient_duplicateNameAndBirthdate_throwsConflict() {
        PatientRequest request = buildPatientRequest();
        PatientEntity existing = patientEntityWith(UUID.randomUUID());
        when(patientRepository.findByDpi(anyString())).thenReturn(Optional.empty());
        when(patientRepository.findByNameAndBirthdate(anyString(), anyString(), any()))
                .thenReturn(List.of(existing));

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> patientService.createPatient(request));

        assertEquals(409, ex.getResponse().getStatus());
    }

    @Test
    void createPatient_noDpi_skipsDpiCheck() {
        PatientRequest request = buildPatientRequest();
        request.setDpi(null);
        when(patientRepository.findByNameAndBirthdate(anyString(), anyString(), any())).thenReturn(List.of());

        patientService.createPatient(request);

        verify(patientRepository, never()).findByDpi(anyString());
        verify(patientRepository).persist(any(PatientEntity.class));
    }

    // ========== deletePatient ==========

    @Test
    void deletePatient_noRelatedData_deletesSuccessfully() {
        UUID patientId = UUID.randomUUID();
        PatientEntity patient = patientEntityWith(patientId);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        // Mockito default para long es 0L — ningún repo retorna datos relacionados

        assertDoesNotThrow(() -> patientService.deletePatient(patientId));
        verify(patientRepository).delete(patient);
    }

    @Test
    void deletePatient_hasAppointments_throwsIllegalState() {
        UUID patientId = UUID.randomUUID();
        PatientEntity patient = patientEntityWith(patientId);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        // any(Object[].class) fuerza el overload count(String, Object...) sin ambigüedad con Map/Parameters
        doReturn(2L).when(medicalAppointmentRepository).count(anyString(), any(Object[].class));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> patientService.deletePatient(patientId));

        assertTrue(ex.getMessage().contains("citas médicas"));
        verify(patientRepository, never()).delete(any(PatientEntity.class));
    }

    @Test
    void deletePatient_hasMedicalRecords_throwsIllegalState() {
        UUID patientId = UUID.randomUUID();
        PatientEntity patient = patientEntityWith(patientId);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        // medicalRecordRepository retorna 1, los demás retornan 0L por defecto
        doReturn(1L).when(medicalRecordRepository).count(anyString(), any(Object[].class));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> patientService.deletePatient(patientId));

        assertTrue(ex.getMessage().contains("expedientes médicos"));
    }

    @Test
    void deletePatient_notFound_throwsNotFoundException() {
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> patientService.deletePatient(patientId));
    }

    // ========== getPatientById ==========

    @Test
    void getPatientById_found_returnsDto() {
        UUID patientId = UUID.randomUUID();
        PatientEntity patient = patientEntityWith(patientId);
        patient.setFirstName("Ana");
        patient.setLastName("Lopez");
        patient.setBirthdate(LocalDate.of(1990, 1, 1));
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));

        PatientDto result = patientService.getPatientById(patientId);

        assertEquals(patientId, result.getId());
        assertEquals("Ana", result.getFirstName());
    }

    @Test
    void getPatientById_notFound_throwsNotFoundException() {
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> patientService.getPatientById(patientId));
    }

    // ========== searchPatients ==========

    @Test
    void searchPatients_validTerm_returnsList() {
        PatientEntity patient = patientEntityWith(UUID.randomUUID());
        patient.setFirstName("Maria");
        patient.setLastName("Gonzalez");
        patient.setBirthdate(LocalDate.of(1985, 5, 20));
        when(patientRepository.searchByTerm("mari")).thenReturn(List.of(patient));

        List<PatientSearchDto> result = patientService.searchPatients("mari");

        assertEquals(1, result.size());
        assertEquals("Maria", result.get(0).getFirstName());
    }

    @Test
    void searchPatients_termTooShort_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> patientService.searchPatients("a"));
    }

    @Test
    void searchPatients_nullTerm_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> patientService.searchPatients(null));
    }

    @Test
    void searchPatients_twoTokenTerm_delegatesToRepository() {
        PatientEntity patient = patientEntityWith(UUID.randomUUID());
        patient.setFirstName("Maria");
        patient.setLastName("Perez");
        patient.setBirthdate(LocalDate.of(1985, 5, 20));
        when(patientRepository.searchByTerm("maria perez")).thenReturn(List.of(patient));

        List<PatientSearchDto> result = patientService.searchPatients("maria perez");

        assertEquals(1, result.size());
    }

    // ========== getMedicalHistoryPathologicalFamByPatientId ==========

    @Test
    void getMedicalHistoryPathologicalFam_asAdmin_returnsHistory() {
        UUID patientId = UUID.randomUUID();
        PatientEntity patient = patientEntityWith(patientId);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(securityContextService.getCurrentDoctorId()).thenReturn(null); // admin/secretary
        MedicalHistoryPathologicalFamEntity histEntity = buildHistoryEntity(patientId);
        when(medicalHistoryPathologicalFamRepository.findByPatientId(patientId))
                .thenReturn(List.of(histEntity));

        List<MedicalHistoryPathologicalFamDto> result =
                patientService.getMedicalHistoryPathologicalFamByPatientId(patientId);

        assertEquals(1, result.size());
    }

    @Test
    void getMedicalHistoryPathologicalFam_doctorWithAppointment_returnsHistory() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        PatientEntity patient = patientEntityWith(patientId);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(securityContextService.getCurrentDoctorId()).thenReturn(doctorId);
        // any(Object[].class) fuerza el overload count(String, Object...) sin ambigüedad
        doReturn(1L).when(medicalAppointmentRepository).count(anyString(), any(Object[].class));
        when(medicalHistoryPathologicalFamRepository.findByPatientId(patientId)).thenReturn(List.of());

        assertDoesNotThrow(() ->
                patientService.getMedicalHistoryPathologicalFamByPatientId(patientId));
    }

    @Test
    void getMedicalHistoryPathologicalFam_doctorWithoutAppointment_throwsForbidden() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        PatientEntity patient = patientEntityWith(patientId);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(securityContextService.getCurrentDoctorId()).thenReturn(doctorId);
        // count() retorna 0L por defecto → ForbiddenException
        // doReturn(0L) no es necesario, es el valor por defecto de Mockito para long

        assertThrows(ForbiddenException.class,
                () -> patientService.getMedicalHistoryPathologicalFamByPatientId(patientId));
    }

    // ========== createMedicalHistoryPathologicalFam ==========

    @Test
    void createMedicalHistoryPathologicalFam_success_setsHasPathologicalHistoryFlag() {
        UUID patientId = UUID.randomUUID();
        PatientEntity patient = patientEntityWith(patientId);
        patient.setHasPathologicalHistory(false);
        MedicalHistoryPathologicalFamRequest request = new MedicalHistoryPathologicalFamRequest();
        request.setPatientId(patientId);
        request.setMedicalHistoryType("Diabetes");
        request.setDescription("Abuelo paterno");

        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(securityContextService.getCurrentDoctorId()).thenReturn(null); // admin

        patientService.createMedicalHistoryPathologicalFam(request);

        assertTrue(patient.getHasPathologicalHistory(), "Debe marcar el flag en el paciente");
        verify(medicalHistoryPathologicalFamRepository).persist(any(MedicalHistoryPathologicalFamEntity.class));
        verify(patientRepository).persist(patient);
    }

    @Test
    void createMedicalHistoryPathologicalFam_alreadyFlagged_doesNotPersistPatientAgain() {
        UUID patientId = UUID.randomUUID();
        PatientEntity patient = patientEntityWith(patientId);
        patient.setHasPathologicalHistory(true); // ya marcado
        MedicalHistoryPathologicalFamRequest request = new MedicalHistoryPathologicalFamRequest();
        request.setPatientId(patientId);
        request.setMedicalHistoryType("Diabetes");

        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(securityContextService.getCurrentDoctorId()).thenReturn(null);

        patientService.createMedicalHistoryPathologicalFam(request);

        // No debe re-persistir el paciente si el flag ya estaba en true
        verify(patientRepository, never()).persist(patient);
    }

    // ========== helpers ==========

    private PatientRequest buildPatientRequest() {
        PatientRequest r = new PatientRequest();
        r.setFirstName("Juan");
        r.setLastName("Perez");
        r.setBirthdate(LocalDate.of(1990, 6, 15));
        r.setDpi("123456789");
        r.setMaritalStatus("Soltero");
        return r;
    }

    private PatientEntity patientEntityWith(UUID id) {
        PatientEntity e = new PatientEntity();
        e.setId(id); // NOTE: @GeneratedValue IDENTITY — necesitamos setear manualmente en tests
        e.setFirstName("Test");
        e.setLastName("Patient");
        e.setBirthdate(LocalDate.of(1990, 1, 1));
        e.setMaritalStatus("Soltero");
        e.setHasPathologicalHistory(false);
        return e;
    }

    private MedicalHistoryPathologicalFamEntity buildHistoryEntity(UUID patientId) {
        PatientEntity patient = patientEntityWith(patientId);
        MedicalHistoryPathologicalFamEntity e = new MedicalHistoryPathologicalFamEntity();
        e.setId(UUID.randomUUID());
        e.setPatient(patient);
        e.setMedicalHistoryType("Diabetes");
        e.setDescription("Historial familiar");
        return e;
    }
}
