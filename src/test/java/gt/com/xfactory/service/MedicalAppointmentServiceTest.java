package gt.com.xfactory.service;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.entity.enums.*;
import gt.com.xfactory.repository.*;
import gt.com.xfactory.service.impl.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;
import org.junit.jupiter.api.*;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class MedicalAppointmentServiceTest {

    @InjectMock
    PatientRepository patientRepository;

    @InjectMock
    DoctorRepository doctorRepository;

    @InjectMock
    ClinicRepository clinicRepository;

    @InjectMock
    SpecialtyRepository specialtyRepository;

    @InjectMock
    MedicalAppointmentRepository medicalAppointmentRepository;

    @InjectMock
    AppointmentDiagnosisRepository appointmentDiagnosisRepository;

    @InjectMock
    DiagnosisCatalogRepository diagnosisCatalogRepository;

    @InjectMock
    DoctorSpecialtyRepository doctorSpecialtyRepository;

    @InjectMock
    SecurityContextService securityContextService;

    @Inject
    MedicalAppointmentService medicalAppointmentService;

    // ========== getMedicalAppointmentById ==========

    @Test
    void getMedicalAppointmentById_found_returnsDto() {
        UUID id = UUID.randomUUID();
        MedicalAppointmentEntity entity = buildAppointmentEntity(id, AppointmentStatus.scheduled);
        when(medicalAppointmentRepository.findByIdOptional(id)).thenReturn(Optional.of(entity));
        when(medicalAppointmentRepository.findChildFollowUpId(id)).thenReturn(Optional.empty());
        when(doctorSpecialtyRepository.findByDoctorId(any())).thenReturn(List.of());
        when(securityContextService.hasRole("secretary")).thenReturn(false);

        MedicalAppointmentDto result = medicalAppointmentService.getMedicalAppointmentById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("scheduled", result.getStatus());
    }

    @Test
    void getMedicalAppointmentById_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(medicalAppointmentRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> medicalAppointmentService.getMedicalAppointmentById(id));
    }

    // ========== getMedicalAppointmentsByPatientId ==========

    @Test
    void getMedicalAppointmentsByPatientId_asAdmin_returnsAllByPatient() {
        UUID patientId = UUID.randomUUID();
        PatientEntity patient = buildPatient(patientId);
        MedicalAppointmentEntity entity = buildAppointmentEntity(UUID.randomUUID(), AppointmentStatus.scheduled);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(securityContextService.getCurrentDoctorId()).thenReturn(null); // admin no filtra
        when(medicalAppointmentRepository.findByPatientIdWithFilters(eq(patientId), any())).thenReturn(List.of(entity));
        when(medicalAppointmentRepository.findChildFollowUpId(any())).thenReturn(Optional.empty());
        when(doctorSpecialtyRepository.findByDoctorId(any())).thenReturn(List.of());
        when(securityContextService.hasRole("secretary")).thenReturn(false);

        List<MedicalAppointmentDto> result = medicalAppointmentService.getMedicalAppointmentsByPatientId(patientId, null);

        assertEquals(1, result.size());
    }

    @Test
    void getMedicalAppointmentsByPatientId_asDoctor_filtersByDoctorId() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        PatientEntity patient = buildPatient(patientId);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(securityContextService.getCurrentDoctorId()).thenReturn(doctorId);
        when(medicalAppointmentRepository.findByPatientIdWithFilters(eq(patientId), any())).thenReturn(List.of());

        List<MedicalAppointmentDto> result = medicalAppointmentService.getMedicalAppointmentsByPatientId(patientId, null);

        assertEquals(0, result.size());
        // Verificar que el filtro fue modificado para incluir el doctorId del contexto
        verify(securityContextService).getCurrentDoctorId();
    }

    @Test
    void getMedicalAppointmentsByPatientId_patientNotFound_throws404() {
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalAppointmentService.getMedicalAppointmentsByPatientId(patientId, null));
    }

    // ========== createMedicalAppointment ==========

    @Test
    void createMedicalAppointment_noStatus_defaultsToScheduled() {
        MedicalAppointmentRequest request = buildRequest(null);
        stubRepositoriesForCreate(request);
        when(medicalAppointmentRepository.findChildFollowUpId(any())).thenReturn(Optional.empty());
        when(doctorSpecialtyRepository.findByDoctorId(any())).thenReturn(List.of());
        when(securityContextService.hasRole("secretary")).thenReturn(false);

        MedicalAppointmentDto result = medicalAppointmentService.createMedicalAppointment(request);

        assertEquals("scheduled", result.getStatus());
        verify(medicalAppointmentRepository).persist(any(MedicalAppointmentEntity.class));
    }

    @Test
    void createMedicalAppointment_statusConfirmed_setsCheckInTime() {
        MedicalAppointmentRequest request = buildRequest(AppointmentStatus.confirmed);
        stubRepositoriesForCreate(request);
        when(medicalAppointmentRepository.findChildFollowUpId(any())).thenReturn(Optional.empty());
        when(doctorSpecialtyRepository.findByDoctorId(any())).thenReturn(List.of());
        when(securityContextService.hasRole("secretary")).thenReturn(false);

        MedicalAppointmentDto result = medicalAppointmentService.createMedicalAppointment(request);

        assertEquals("confirmed", result.getStatus());
        assertNotNull(result.getCheckInTime());
    }

    @Test
    void createMedicalAppointment_statusInProgress_setsStartTime() {
        MedicalAppointmentRequest request = buildRequest(AppointmentStatus.in_progress);
        stubRepositoriesForCreate(request);
        when(medicalAppointmentRepository.findChildFollowUpId(any())).thenReturn(Optional.empty());
        when(doctorSpecialtyRepository.findByDoctorId(any())).thenReturn(List.of());
        when(securityContextService.hasRole("secretary")).thenReturn(false);

        MedicalAppointmentDto result = medicalAppointmentService.createMedicalAppointment(request);

        assertEquals("in_progress", result.getStatus());
        assertNotNull(result.getStartTime());
    }

    @Test
    void createMedicalAppointment_statusCompleted_setsEndTime() {
        MedicalAppointmentRequest request = buildRequest(AppointmentStatus.completed);
        stubRepositoriesForCreate(request);
        when(medicalAppointmentRepository.findChildFollowUpId(any())).thenReturn(Optional.empty());
        when(doctorSpecialtyRepository.findByDoctorId(any())).thenReturn(List.of());
        when(securityContextService.hasRole("secretary")).thenReturn(false);

        MedicalAppointmentDto result = medicalAppointmentService.createMedicalAppointment(request);

        assertEquals("completed", result.getStatus());
        assertNotNull(result.getEndTime());
    }

    @Test
    void createMedicalAppointment_statusCancelled_setsCancellationReason() {
        MedicalAppointmentRequest request = buildRequest(AppointmentStatus.cancelled);
        request.setCancellationReason("Paciente no disponible");
        stubRepositoriesForCreate(request);
        when(medicalAppointmentRepository.findChildFollowUpId(any())).thenReturn(Optional.empty());
        when(doctorSpecialtyRepository.findByDoctorId(any())).thenReturn(List.of());
        when(securityContextService.hasRole("secretary")).thenReturn(false);

        MedicalAppointmentDto result = medicalAppointmentService.createMedicalAppointment(request);

        assertEquals("cancelled", result.getStatus());
        assertEquals("Paciente no disponible", result.getCancellationReason());
    }

    @Test
    void createMedicalAppointment_withDiagnoses_persistsDiagnoses() {
        MedicalAppointmentRequest request = buildRequest(null);
        AppointmentDiagnosisRequest diagReq = new AppointmentDiagnosisRequest();
        UUID diagId = UUID.randomUUID();
        diagReq.setDiagnosisId(diagId);
        diagReq.setType(DiagnosisType.principal);
        request.setDiagnoses(List.of(diagReq));

        stubRepositoriesForCreate(request);
        DiagnosisCatalogEntity catalog = new DiagnosisCatalogEntity();
        catalog.setId(diagId);
        catalog.setCode("A10");
        catalog.setName("Diabetes");
        when(diagnosisCatalogRepository.findByIdOptional(diagId)).thenReturn(Optional.of(catalog));
        when(medicalAppointmentRepository.findChildFollowUpId(any())).thenReturn(Optional.empty());
        when(doctorSpecialtyRepository.findByDoctorId(any())).thenReturn(List.of());
        when(securityContextService.hasRole("secretary")).thenReturn(false);

        medicalAppointmentService.createMedicalAppointment(request);

        verify(appointmentDiagnosisRepository).persist(any(AppointmentDiagnosisEntity.class));
    }

    @Test
    void createMedicalAppointment_patientNotFound_throws404() {
        MedicalAppointmentRequest request = buildRequest(null);
        when(patientRepository.findByIdOptional(request.getPatientId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalAppointmentService.createMedicalAppointment(request));
    }

    @Test
    void createMedicalAppointment_doctorNotFound_throws404() {
        MedicalAppointmentRequest request = buildRequest(null);
        UUID patientId = request.getPatientId();
        UUID doctorId = request.getDoctorId();
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(buildPatient(patientId)));
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalAppointmentService.createMedicalAppointment(request));
    }

    @Test
    void createMedicalAppointment_clinicNotFound_throws404() {
        MedicalAppointmentRequest request = buildRequest(null);
        UUID patientId = request.getPatientId();
        UUID doctorId = request.getDoctorId();
        UUID clinicId = request.getClinicId();
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(buildPatient(patientId)));
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(buildDoctor(doctorId)));
        when(clinicRepository.findByIdOptional(clinicId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalAppointmentService.createMedicalAppointment(request));
    }

    // ========== updateMedicalAppointment ==========

    @Test
    void updateMedicalAppointment_modifiableStatus_updatesSuccessfully() {
        UUID id = UUID.randomUUID();
        MedicalAppointmentEntity entity = buildAppointmentEntity(id, AppointmentStatus.scheduled);
        MedicalAppointmentRequest request = buildRequest(null);
        when(medicalAppointmentRepository.findByIdOptional(id)).thenReturn(Optional.of(entity));
        stubRepositoriesForCreate(request);
        when(medicalAppointmentRepository.findChildFollowUpId(any())).thenReturn(Optional.empty());
        when(doctorSpecialtyRepository.findByDoctorId(any())).thenReturn(List.of());
        when(securityContextService.hasRole("secretary")).thenReturn(false);

        MedicalAppointmentDto result = medicalAppointmentService.updateMedicalAppointment(id, request);

        assertNotNull(result);
        verify(medicalAppointmentRepository).persist(entity);
    }

    @Test
    void updateMedicalAppointment_nonModifiableStatus_throwsIllegalState() {
        UUID id = UUID.randomUUID();
        MedicalAppointmentEntity entity = buildAppointmentEntity(id, AppointmentStatus.completed);
        when(medicalAppointmentRepository.findByIdOptional(id)).thenReturn(Optional.of(entity));

        assertThrows(IllegalStateException.class,
                () -> medicalAppointmentService.updateMedicalAppointment(id, buildRequest(null)));
    }

    @Test
    void updateMedicalAppointment_cancelledStatus_throwsIllegalState() {
        UUID id = UUID.randomUUID();
        MedicalAppointmentEntity entity = buildAppointmentEntity(id, AppointmentStatus.cancelled);
        when(medicalAppointmentRepository.findByIdOptional(id)).thenReturn(Optional.of(entity));

        assertThrows(IllegalStateException.class,
                () -> medicalAppointmentService.updateMedicalAppointment(id, buildRequest(null)));
    }

    @Test
    void updateMedicalAppointment_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(medicalAppointmentRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalAppointmentService.updateMedicalAppointment(id, buildRequest(null)));
    }

    // ========== reopenMedicalAppointment ==========

    @Test
    void reopenMedicalAppointment_expired_setsReopenedAndNewDate() {
        UUID id = UUID.randomUUID();
        MedicalAppointmentEntity entity = buildAppointmentEntity(id, AppointmentStatus.expired);
        ReopenAppointmentRequest request = new ReopenAppointmentRequest();
        request.setAppointmentDate(LocalDateTime.now().plusDays(3));
        when(medicalAppointmentRepository.findByIdOptional(id)).thenReturn(Optional.of(entity));
        when(medicalAppointmentRepository.findChildFollowUpId(any())).thenReturn(Optional.empty());
        when(doctorSpecialtyRepository.findByDoctorId(any())).thenReturn(List.of());
        when(securityContextService.hasRole("secretary")).thenReturn(false);

        MedicalAppointmentDto result = medicalAppointmentService.reopenMedicalAppointment(id, request);

        assertEquals("reopened", result.getStatus());
        verify(medicalAppointmentRepository).persist(entity);
    }

    @Test
    void reopenMedicalAppointment_nonExpired_throwsIllegalState() {
        UUID id = UUID.randomUUID();
        MedicalAppointmentEntity entity = buildAppointmentEntity(id, AppointmentStatus.scheduled);
        when(medicalAppointmentRepository.findByIdOptional(id)).thenReturn(Optional.of(entity));

        ReopenAppointmentRequest request = new ReopenAppointmentRequest();
        request.setAppointmentDate(LocalDateTime.now().plusDays(3));

        assertThrows(IllegalStateException.class,
                () -> medicalAppointmentService.reopenMedicalAppointment(id, request));
    }

    @Test
    void reopenMedicalAppointment_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(medicalAppointmentRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        ReopenAppointmentRequest request = new ReopenAppointmentRequest();
        request.setAppointmentDate(LocalDateTime.now().plusDays(1));

        assertThrows(NotFoundException.class,
                () -> medicalAppointmentService.reopenMedicalAppointment(id, request));
    }

    // ========== deleteMedicalAppointment ==========

    @Test
    void deleteMedicalAppointment_modifiableStatus_deletesSuccessfully() {
        UUID id = UUID.randomUUID();
        MedicalAppointmentEntity entity = buildAppointmentEntity(id, AppointmentStatus.scheduled);
        when(medicalAppointmentRepository.findByIdOptional(id)).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> medicalAppointmentService.deleteMedicalAppointment(id));
        verify(appointmentDiagnosisRepository).deleteByAppointmentId(id);
        verify(medicalAppointmentRepository).delete(entity);
    }

    @Test
    void deleteMedicalAppointment_nonModifiableStatus_throwsIllegalState() {
        UUID id = UUID.randomUUID();
        MedicalAppointmentEntity entity = buildAppointmentEntity(id, AppointmentStatus.no_show);
        when(medicalAppointmentRepository.findByIdOptional(id)).thenReturn(Optional.of(entity));

        assertThrows(IllegalStateException.class,
                () -> medicalAppointmentService.deleteMedicalAppointment(id));
        verify(medicalAppointmentRepository, never()).delete(any(MedicalAppointmentEntity.class));
    }

    @Test
    void deleteMedicalAppointment_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(medicalAppointmentRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalAppointmentService.deleteMedicalAppointment(id));
    }

    // ========== toMedicalAppointmentDto — secretary privacy ==========

    @Test
    void toMedicalAppointmentDto_asSecretary_redactsClinicalFields() {
        UUID id = UUID.randomUUID();
        MedicalAppointmentEntity entity = buildAppointmentEntity(id, AppointmentStatus.scheduled);
        entity.setDiagnosis("Hipertensión");
        entity.setNotes("Notas privadas");
        entity.setDiagnoses(List.of());
        when(medicalAppointmentRepository.findByIdOptional(id)).thenReturn(Optional.of(entity));
        when(medicalAppointmentRepository.findChildFollowUpId(any())).thenReturn(Optional.empty());
        when(doctorSpecialtyRepository.findByDoctorId(any())).thenReturn(List.of());
        when(securityContextService.hasRole("secretary")).thenReturn(true);
        when(securityContextService.hasRole("admin")).thenReturn(false);

        MedicalAppointmentDto dto = medicalAppointmentService.getMedicalAppointmentById(id);

        assertNull(dto.getDiagnosis(), "Secretary no debe ver diagnosis");
        assertNull(dto.getNotes(), "Secretary no debe ver notes");
        assertNull(dto.getDiagnoses(), "Secretary no debe ver lista de diagnósticos");
    }

    @Test
    void toMedicalAppointmentDto_asDoctor_includesClinicalFields() {
        UUID id = UUID.randomUUID();
        MedicalAppointmentEntity entity = buildAppointmentEntity(id, AppointmentStatus.scheduled);
        entity.setDiagnosis("Hipertensión");
        entity.setNotes("Notas privadas");
        entity.setDiagnoses(List.of());
        when(medicalAppointmentRepository.findByIdOptional(id)).thenReturn(Optional.of(entity));
        when(medicalAppointmentRepository.findChildFollowUpId(any())).thenReturn(Optional.empty());
        when(doctorSpecialtyRepository.findByDoctorId(any())).thenReturn(List.of());
        when(securityContextService.hasRole("secretary")).thenReturn(false);

        MedicalAppointmentDto dto = medicalAppointmentService.getMedicalAppointmentById(id);

        assertEquals("Hipertensión", dto.getDiagnosis());
        assertEquals("Notas privadas", dto.getNotes());
    }

    // ========== helpers ==========

    private MedicalAppointmentEntity buildAppointmentEntity(UUID id, AppointmentStatus status) {
        MedicalAppointmentEntity e = new MedicalAppointmentEntity();
        e.setId(id);
        e.setPatient(buildPatient(UUID.randomUUID()));
        e.setDoctor(buildDoctor(UUID.randomUUID()));
        e.setClinic(buildClinic(UUID.randomUUID()));
        e.setStatus(status);
        e.setAppointmentDate(LocalDateTime.now().plusDays(1));
        return e;
    }

    private PatientEntity buildPatient(UUID id) {
        PatientEntity p = new PatientEntity();
        p.setId(id);
        p.setFirstName("María");
        p.setLastName("García");
        p.setBirthdate(LocalDate.of(1990, 1, 1));
        return p;
    }

    private DoctorEntity buildDoctor(UUID id) {
        DoctorEntity d = new DoctorEntity();
        d.setId(id);
        d.setFirstName("Dr. Carlos");
        d.setLastName("López");
        return d;
    }

    private ClinicEntity buildClinic(UUID id) {
        ClinicEntity c = new ClinicEntity();
        c.setId(id);
        c.setName("Clínica Central");
        return c;
    }

    private MedicalAppointmentRequest buildRequest(AppointmentStatus status) {
        MedicalAppointmentRequest r = new MedicalAppointmentRequest();
        r.setPatientId(UUID.randomUUID());
        r.setDoctorId(UUID.randomUUID());
        r.setClinicId(UUID.randomUUID());
        r.setAppointmentDate(LocalDateTime.now().plusDays(1));
        r.setStatus(status);
        return r;
    }

    private void stubRepositoriesForCreate(MedicalAppointmentRequest request) {
        when(patientRepository.findByIdOptional(request.getPatientId()))
                .thenReturn(Optional.of(buildPatient(request.getPatientId())));
        when(doctorRepository.findByIdOptional(request.getDoctorId()))
                .thenReturn(Optional.of(buildDoctor(request.getDoctorId())));
        when(clinicRepository.findByIdOptional(request.getClinicId()))
                .thenReturn(Optional.of(buildClinic(request.getClinicId())));
    }
}
