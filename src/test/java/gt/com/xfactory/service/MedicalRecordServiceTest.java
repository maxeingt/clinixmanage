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

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class MedicalRecordServiceTest {

    @InjectMock
    MedicalRecordRepository medicalRecordRepository;

    @InjectMock
    PatientRepository patientRepository;

    @InjectMock
    DoctorRepository doctorRepository;

    @InjectMock
    MedicalAppointmentRepository medicalAppointmentRepository;

    @InjectMock
    SpecialtyRepository specialtyRepository;

    @InjectMock
    SpecialtyFormTemplateRepository specialtyFormTemplateRepository;

    @InjectMock
    SecurityContextService securityContextService;

    @Inject
    MedicalRecordService medicalRecordService;

    // ========== getMedicalRecordsByPatientId ==========

    @Test
    void getMedicalRecordsByPatientId_asAdmin_callsFindByPatientId() {
        UUID patientId = UUID.randomUUID();
        PatientEntity patient = buildPatient(patientId);
        MedicalRecordEntity record = buildRecord(UUID.randomUUID(), patient, buildDoctor(UUID.randomUUID()));
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(securityContextService.getCurrentDoctorId()).thenReturn(null);
        when(medicalRecordRepository.findByPatientId(patientId)).thenReturn(List.of(record));

        List<MedicalRecordDto> result = medicalRecordService.getMedicalRecordsByPatientId(patientId);

        assertEquals(1, result.size());
        verify(medicalRecordRepository).findByPatientId(patientId);
        verify(medicalRecordRepository, never()).findByPatientIdAndDoctorId(any(), any());
    }

    @Test
    void getMedicalRecordsByPatientId_asDoctor_callsFindByPatientIdAndDoctorId() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        PatientEntity patient = buildPatient(patientId);
        MedicalRecordEntity record = buildRecord(UUID.randomUUID(), patient, buildDoctor(doctorId));
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(securityContextService.getCurrentDoctorId()).thenReturn(doctorId);
        when(medicalRecordRepository.findByPatientIdAndDoctorId(patientId, doctorId)).thenReturn(List.of(record));

        List<MedicalRecordDto> result = medicalRecordService.getMedicalRecordsByPatientId(patientId);

        assertEquals(1, result.size());
        verify(medicalRecordRepository).findByPatientIdAndDoctorId(patientId, doctorId);
        verify(medicalRecordRepository, never()).findByPatientId(any());
    }

    @Test
    void getMedicalRecordsByPatientId_patientNotFound_throws404() {
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalRecordService.getMedicalRecordsByPatientId(patientId));
    }

    // ========== getMedicalRecordsByAppointmentId ==========

    @Test
    void getMedicalRecordsByAppointmentId_ownerDoctor_returnsRecords() {
        UUID appointmentId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        MedicalAppointmentEntity appointment = buildAppointment(appointmentId, doctor);
        MedicalRecordEntity record = buildRecord(UUID.randomUUID(), buildPatient(UUID.randomUUID()), doctor);
        when(medicalAppointmentRepository.findByIdOptional(appointmentId)).thenReturn(Optional.of(appointment));
        doNothing().when(securityContextService).validateDoctorOwnership(doctorId);
        when(medicalRecordRepository.findByAppointmentId(appointmentId)).thenReturn(List.of(record));

        List<MedicalRecordDto> result = medicalRecordService.getMedicalRecordsByAppointmentId(appointmentId);

        assertEquals(1, result.size());
    }

    @Test
    void getMedicalRecordsByAppointmentId_differentDoctor_throwsForbidden() {
        UUID appointmentId = UUID.randomUUID();
        UUID otherDoctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(otherDoctorId);
        MedicalAppointmentEntity appointment = buildAppointment(appointmentId, doctor);
        when(medicalAppointmentRepository.findByIdOptional(appointmentId)).thenReturn(Optional.of(appointment));
        doThrow(new ForbiddenException("No tiene acceso"))
                .when(securityContextService).validateDoctorOwnership(otherDoctorId);

        assertThrows(ForbiddenException.class,
                () -> medicalRecordService.getMedicalRecordsByAppointmentId(appointmentId));
    }

    @Test
    void getMedicalRecordsByAppointmentId_appointmentNotFound_throws404() {
        UUID appointmentId = UUID.randomUUID();
        when(medicalAppointmentRepository.findByIdOptional(appointmentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalRecordService.getMedicalRecordsByAppointmentId(appointmentId));
    }

    // ========== getMedicalRecordById ==========

    @Test
    void getMedicalRecordById_ownerDoctor_returnsDto() {
        UUID recordId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        MedicalRecordEntity record = buildRecord(recordId, buildPatient(UUID.randomUUID()), doctor);
        when(medicalRecordRepository.findByIdOptional(recordId)).thenReturn(Optional.of(record));
        doNothing().when(securityContextService).validateDoctorOwnership(doctorId);

        MedicalRecordDto result = medicalRecordService.getMedicalRecordById(recordId);

        assertNotNull(result);
        assertEquals(recordId, result.getId());
    }

    @Test
    void getMedicalRecordById_differentDoctor_throwsForbidden() {
        UUID recordId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        MedicalRecordEntity record = buildRecord(recordId, buildPatient(UUID.randomUUID()), doctor);
        when(medicalRecordRepository.findByIdOptional(recordId)).thenReturn(Optional.of(record));
        doThrow(new ForbiddenException("No tiene acceso"))
                .when(securityContextService).validateDoctorOwnership(doctorId);

        assertThrows(ForbiddenException.class,
                () -> medicalRecordService.getMedicalRecordById(recordId));
    }

    @Test
    void getMedicalRecordById_notFound_throws404() {
        UUID recordId = UUID.randomUUID();
        when(medicalRecordRepository.findByIdOptional(recordId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalRecordService.getMedicalRecordById(recordId));
    }

    // ========== createMedicalRecord ==========

    @Test
    void createMedicalRecord_minimal_persistsAndReturnsDto() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        MedicalRecordRequest request = buildRecordRequest(patientId, doctorId);
        PatientEntity patient = buildPatient(patientId);
        DoctorEntity doctor = buildDoctor(doctorId);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(doctor));

        MedicalRecordDto result = medicalRecordService.createMedicalRecord(request);

        assertNotNull(result);
        assertEquals(patientId, result.getPatientId());
        verify(medicalRecordRepository).persist(any(MedicalRecordEntity.class));
    }

    @Test
    void createMedicalRecord_withFormTemplate_setsTemplateIdAndVersion() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        MedicalRecordRequest request = buildRecordRequest(patientId, doctorId);
        request.setFormTemplateId(templateId);
        PatientEntity patient = buildPatient(patientId);
        DoctorEntity doctor = buildDoctor(doctorId);
        SpecialtyFormTemplateEntity template = new SpecialtyFormTemplateEntity();
        template.setId(templateId);
        template.setVersion(2);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(doctor));
        when(specialtyFormTemplateRepository.findByIdOptional(templateId)).thenReturn(Optional.of(template));

        MedicalRecordDto result = medicalRecordService.createMedicalRecord(request);

        assertEquals(templateId, result.getFormTemplateId());
        assertEquals(2, result.getFormTemplateVersion());
    }

    @Test
    void createMedicalRecord_withAppointment_setsAppointmentId() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        MedicalRecordRequest request = buildRecordRequest(patientId, doctorId);
        request.setAppointmentId(appointmentId);
        PatientEntity patient = buildPatient(patientId);
        DoctorEntity doctor = buildDoctor(doctorId);
        MedicalAppointmentEntity appointment = buildAppointment(appointmentId, doctor);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(doctor));
        when(medicalAppointmentRepository.findByIdOptional(appointmentId)).thenReturn(Optional.of(appointment));

        MedicalRecordDto result = medicalRecordService.createMedicalRecord(request);

        assertEquals(appointmentId, result.getAppointmentId());
    }

    @Test
    void createMedicalRecord_withSpecialty_setsSpecialty() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();
        MedicalRecordRequest request = buildRecordRequest(patientId, doctorId);
        request.setSpecialtyId(specialtyId);
        PatientEntity patient = buildPatient(patientId);
        DoctorEntity doctor = buildDoctor(doctorId);
        SpecialtyEntity specialty = new SpecialtyEntity();
        specialty.setId(specialtyId);
        specialty.setName("Cardiología");
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(doctor));
        when(specialtyRepository.findByIdOptional(specialtyId)).thenReturn(Optional.of(specialty));

        MedicalRecordDto result = medicalRecordService.createMedicalRecord(request);

        assertEquals(specialtyId, result.getSpecialtyId());
        assertEquals("Cardiología", result.getSpecialtyName());
    }

    @Test
    void createMedicalRecord_patientNotFound_throws404() {
        UUID patientId = UUID.randomUUID();
        MedicalRecordRequest request = buildRecordRequest(patientId, UUID.randomUUID());
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalRecordService.createMedicalRecord(request));
    }

    @Test
    void createMedicalRecord_doctorNotFound_throws404() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        MedicalRecordRequest request = buildRecordRequest(patientId, doctorId);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(buildPatient(patientId)));
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalRecordService.createMedicalRecord(request));
    }

    @Test
    void createMedicalRecord_templateNotFound_throws404() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        MedicalRecordRequest request = buildRecordRequest(patientId, doctorId);
        request.setFormTemplateId(templateId);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(buildPatient(patientId)));
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(buildDoctor(doctorId)));
        when(specialtyFormTemplateRepository.findByIdOptional(templateId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalRecordService.createMedicalRecord(request));
    }

    // ========== updateMedicalRecord ==========

    @Test
    void updateMedicalRecord_ownerDoctor_updatesPartialFields() {
        UUID recordId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        MedicalRecordEntity record = buildRecord(recordId, buildPatient(UUID.randomUUID()), doctor);
        record.setChiefComplaint("Motivo original");
        MedicalRecordRequest request = buildRecordRequest(record.getPatient().getId(), doctorId);
        request.setChiefComplaint("Motivo actualizado");
        when(medicalRecordRepository.findByIdOptional(recordId)).thenReturn(Optional.of(record));
        doNothing().when(securityContextService).validateDoctorOwnership(doctorId);

        MedicalRecordDto result = medicalRecordService.updateMedicalRecord(recordId, request);

        assertEquals("Motivo actualizado", result.getChiefComplaint());
        verify(medicalRecordRepository).persist(record);
    }

    @Test
    void updateMedicalRecord_nullFields_doesNotOverwriteExistingValues() {
        UUID recordId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        MedicalRecordEntity record = buildRecord(recordId, buildPatient(UUID.randomUUID()), doctor);
        record.setChiefComplaint("Motivo original");
        record.setTreatmentPlan("Plan original");
        MedicalRecordRequest request = buildRecordRequest(record.getPatient().getId(), doctorId);
        // Solo actualiza chiefComplaint, TreatmentPlan queda null en el request
        request.setChiefComplaint("Nuevo motivo");
        request.setTreatmentPlan(null);
        when(medicalRecordRepository.findByIdOptional(recordId)).thenReturn(Optional.of(record));
        doNothing().when(securityContextService).validateDoctorOwnership(doctorId);

        medicalRecordService.updateMedicalRecord(recordId, request);

        // treatmentPlan no debe ser sobreescrito con null
        assertEquals("Plan original", record.getTreatmentPlan());
    }

    @Test
    void updateMedicalRecord_differentDoctor_throwsForbidden() {
        UUID recordId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        MedicalRecordEntity record = buildRecord(recordId, buildPatient(UUID.randomUUID()), doctor);
        when(medicalRecordRepository.findByIdOptional(recordId)).thenReturn(Optional.of(record));
        doThrow(new ForbiddenException("No tiene acceso"))
                .when(securityContextService).validateDoctorOwnership(doctorId);

        assertThrows(ForbiddenException.class,
                () -> medicalRecordService.updateMedicalRecord(recordId, buildRecordRequest(UUID.randomUUID(), doctorId)));
    }

    @Test
    void updateMedicalRecord_notFound_throws404() {
        UUID recordId = UUID.randomUUID();
        when(medicalRecordRepository.findByIdOptional(recordId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalRecordService.updateMedicalRecord(recordId, buildRecordRequest(UUID.randomUUID(), UUID.randomUUID())));
    }

    // ========== deleteMedicalRecord ==========

    @Test
    void deleteMedicalRecord_ownerDoctor_deletesSuccessfully() {
        UUID recordId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        MedicalRecordEntity record = buildRecord(recordId, buildPatient(UUID.randomUUID()), doctor);
        when(medicalRecordRepository.findByIdOptional(recordId)).thenReturn(Optional.of(record));
        doNothing().when(securityContextService).validateDoctorOwnership(doctorId);

        assertDoesNotThrow(() -> medicalRecordService.deleteMedicalRecord(recordId));
        verify(medicalRecordRepository).delete(record);
    }

    @Test
    void deleteMedicalRecord_differentDoctor_throwsForbidden() {
        UUID recordId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        MedicalRecordEntity record = buildRecord(recordId, buildPatient(UUID.randomUUID()), doctor);
        when(medicalRecordRepository.findByIdOptional(recordId)).thenReturn(Optional.of(record));
        doThrow(new ForbiddenException("No tiene acceso"))
                .when(securityContextService).validateDoctorOwnership(doctorId);

        assertThrows(ForbiddenException.class,
                () -> medicalRecordService.deleteMedicalRecord(recordId));
        verify(medicalRecordRepository, never()).delete(any(MedicalRecordEntity.class));
    }

    @Test
    void deleteMedicalRecord_notFound_throws404() {
        UUID recordId = UUID.randomUUID();
        when(medicalRecordRepository.findByIdOptional(recordId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalRecordService.deleteMedicalRecord(recordId));
    }

    // ========== helpers ==========

    private PatientEntity buildPatient(UUID id) {
        PatientEntity p = new PatientEntity();
        p.setId(id);
        p.setFirstName("Ana");
        p.setLastName("Martínez");
        p.setBirthdate(LocalDate.of(1985, 3, 10));
        return p;
    }

    private DoctorEntity buildDoctor(UUID id) {
        DoctorEntity d = new DoctorEntity();
        d.setId(id);
        d.setFirstName("Dr. Luis");
        d.setLastName("Ramírez");
        return d;
    }

    private MedicalRecordEntity buildRecord(UUID id, PatientEntity patient, DoctorEntity doctor) {
        MedicalRecordEntity r = new MedicalRecordEntity();
        r.setId(id);
        r.setPatient(patient);
        r.setDoctor(doctor);
        r.setChiefComplaint("Dolor de cabeza");
        return r;
    }

    private MedicalAppointmentEntity buildAppointment(UUID id, DoctorEntity doctor) {
        MedicalAppointmentEntity a = new MedicalAppointmentEntity();
        a.setId(id);
        a.setDoctor(doctor);
        return a;
    }

    private MedicalRecordRequest buildRecordRequest(UUID patientId, UUID doctorId) {
        MedicalRecordRequest r = new MedicalRecordRequest();
        r.setPatientId(patientId);
        r.setDoctorId(doctorId);
        r.setChiefComplaint("Cefalea intensa");
        return r;
    }
}
