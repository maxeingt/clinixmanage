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
class LabOrderServiceTest {

    @InjectMock
    LabOrderRepository labOrderRepository;

    @InjectMock
    LabResultRepository labResultRepository;

    @InjectMock
    LabOrderAttachmentRepository labOrderAttachmentRepository;

    @InjectMock
    FileStorageService fileStorageService;

    @InjectMock
    PatientRepository patientRepository;

    @InjectMock
    DoctorRepository doctorRepository;

    @InjectMock
    MedicalAppointmentRepository medicalAppointmentRepository;

    @InjectMock
    SecurityContextService securityContextService;

    @Inject
    LabOrderService labOrderService;

    // ========== getLabOrdersByPatientId ==========

    @Test
    void getLabOrdersByPatientId_asAdmin_callsFindByPatientId() {
        UUID patientId = UUID.randomUUID();
        PatientEntity patient = buildPatient(patientId);
        LabOrderEntity order = buildOrder(UUID.randomUUID(), patient, buildDoctor(UUID.randomUUID()));
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(securityContextService.getCurrentDoctorId()).thenReturn(null);
        when(labOrderRepository.findByPatientId(patientId)).thenReturn(List.of(order));

        List<LabOrderDto> result = labOrderService.getLabOrdersByPatientId(patientId);

        assertEquals(1, result.size());
        verify(labOrderRepository).findByPatientId(patientId);
        verify(labOrderRepository, never()).findByPatientIdAndDoctorId(any(), any());
    }

    @Test
    void getLabOrdersByPatientId_asDoctor_callsFindByPatientIdAndDoctorId() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        PatientEntity patient = buildPatient(patientId);
        DoctorEntity doctor = buildDoctor(doctorId);
        LabOrderEntity order = buildOrder(UUID.randomUUID(), patient, doctor);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(securityContextService.getCurrentDoctorId()).thenReturn(doctorId);
        when(labOrderRepository.findByPatientIdAndDoctorId(patientId, doctorId)).thenReturn(List.of(order));

        List<LabOrderDto> result = labOrderService.getLabOrdersByPatientId(patientId);

        assertEquals(1, result.size());
        verify(labOrderRepository).findByPatientIdAndDoctorId(patientId, doctorId);
        verify(labOrderRepository, never()).findByPatientId(any());
    }

    @Test
    void getLabOrdersByPatientId_patientNotFound_throws404() {
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> labOrderService.getLabOrdersByPatientId(patientId));
    }

    // ========== getLabOrderById ==========

    @Test
    void getLabOrderById_ownerDoctor_returnsDto() {
        UUID id = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        LabOrderEntity order = buildOrder(id, buildPatient(UUID.randomUUID()), doctor);
        when(labOrderRepository.findByIdOptional(id)).thenReturn(Optional.of(order));
        doNothing().when(securityContextService).validateDoctorOwnership(doctorId);

        LabOrderDto result = labOrderService.getLabOrderById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void getLabOrderById_differentDoctor_throwsForbidden() {
        UUID id = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        LabOrderEntity order = buildOrder(id, buildPatient(UUID.randomUUID()), doctor);
        when(labOrderRepository.findByIdOptional(id)).thenReturn(Optional.of(order));
        doThrow(new ForbiddenException("No tiene acceso"))
                .when(securityContextService).validateDoctorOwnership(doctorId);

        assertThrows(ForbiddenException.class,
                () -> labOrderService.getLabOrderById(id));
    }

    @Test
    void getLabOrderById_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(labOrderRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> labOrderService.getLabOrderById(id));
    }

    // ========== createLabOrder ==========

    @Test
    void createLabOrder_noResults_createsOrderOnly() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        LabOrderRequest request = buildOrderRequest(patientId, doctorId);
        PatientEntity patient = buildPatient(patientId);
        DoctorEntity doctor = buildDoctor(doctorId);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(doctor));

        LabOrderDto result = labOrderService.createLabOrder(request);

        assertNotNull(result);
        verify(labOrderRepository).persist(any(LabOrderEntity.class));
        verify(labResultRepository, never()).persist(any(LabResultEntity.class));
    }

    @Test
    void createLabOrder_withResults_persistsResults() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        LabOrderRequest request = buildOrderRequest(patientId, doctorId);
        LabResultRequest resultReq = new LabResultRequest();
        resultReq.setTestName("Hemograma");
        resultReq.setIsAbnormal(false);
        request.setResults(List.of(resultReq));
        PatientEntity patient = buildPatient(patientId);
        DoctorEntity doctor = buildDoctor(doctorId);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(doctor));

        LabOrderDto result = labOrderService.createLabOrder(request);

        assertNotNull(result);
        verify(labResultRepository).persist(any(LabResultEntity.class));
    }

    @Test
    void createLabOrder_withAppointment_setsAppointmentId() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        LabOrderRequest request = buildOrderRequest(patientId, doctorId);
        request.setAppointmentId(appointmentId);
        DoctorEntity doctor = buildDoctor(doctorId);
        MedicalAppointmentEntity appointment = buildAppointment(appointmentId, doctor);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(buildPatient(patientId)));
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(doctor));
        when(medicalAppointmentRepository.findByIdOptional(appointmentId)).thenReturn(Optional.of(appointment));

        LabOrderDto result = labOrderService.createLabOrder(request);

        assertEquals(appointmentId, result.getAppointmentId());
    }

    @Test
    void createLabOrder_patientNotFound_throws404() {
        UUID patientId = UUID.randomUUID();
        LabOrderRequest request = buildOrderRequest(patientId, UUID.randomUUID());
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> labOrderService.createLabOrder(request));
    }

    @Test
    void createLabOrder_doctorNotFound_throws404() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        LabOrderRequest request = buildOrderRequest(patientId, doctorId);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(buildPatient(patientId)));
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> labOrderService.createLabOrder(request));
    }

    @Test
    void createLabOrder_appointmentNotFound_throws404() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        LabOrderRequest request = buildOrderRequest(patientId, doctorId);
        request.setAppointmentId(appointmentId);
        when(patientRepository.findByIdOptional(patientId)).thenReturn(Optional.of(buildPatient(patientId)));
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(buildDoctor(doctorId)));
        when(medicalAppointmentRepository.findByIdOptional(appointmentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> labOrderService.createLabOrder(request));
    }

    // ========== updateStatus ==========

    @Test
    void updateStatus_ownerDoctor_updatesStatus() {
        UUID id = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        LabOrderEntity order = buildOrder(id, buildPatient(UUID.randomUUID()), doctor);
        order.setStatus(LabOrderStatus.pending);
        when(labOrderRepository.findByIdOptional(id)).thenReturn(Optional.of(order));
        doNothing().when(securityContextService).validateDoctorOwnership(doctorId);

        LabOrderDto result = labOrderService.updateStatus(id, LabOrderStatus.in_progress);

        assertEquals(LabOrderStatus.in_progress, result.getStatus());
        verify(labOrderRepository).persist(order);
    }

    @Test
    void updateStatus_differentDoctor_throwsForbidden() {
        UUID id = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        LabOrderEntity order = buildOrder(id, buildPatient(UUID.randomUUID()), doctor);
        when(labOrderRepository.findByIdOptional(id)).thenReturn(Optional.of(order));
        doThrow(new ForbiddenException("No tiene acceso"))
                .when(securityContextService).validateDoctorOwnership(doctorId);

        assertThrows(ForbiddenException.class,
                () -> labOrderService.updateStatus(id, LabOrderStatus.completed));
    }

    @Test
    void updateStatus_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(labOrderRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> labOrderService.updateStatus(id, LabOrderStatus.in_progress));
    }

    // ========== deleteLabOrder ==========

    @Test
    void deleteLabOrder_ownerDoctor_deletesSuccessfully() {
        UUID id = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        LabOrderEntity order = buildOrder(id, buildPatient(UUID.randomUUID()), doctor);
        when(labOrderRepository.findByIdOptional(id)).thenReturn(Optional.of(order));
        doNothing().when(securityContextService).validateDoctorOwnership(doctorId);

        assertDoesNotThrow(() -> labOrderService.deleteLabOrder(id));
        verify(labOrderRepository).delete(order);
    }

    @Test
    void deleteLabOrder_differentDoctor_throwsForbidden() {
        UUID id = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        LabOrderEntity order = buildOrder(id, buildPatient(UUID.randomUUID()), doctor);
        when(labOrderRepository.findByIdOptional(id)).thenReturn(Optional.of(order));
        doThrow(new ForbiddenException("No tiene acceso"))
                .when(securityContextService).validateDoctorOwnership(doctorId);

        assertThrows(ForbiddenException.class,
                () -> labOrderService.deleteLabOrder(id));
        verify(labOrderRepository, never()).delete(any(LabOrderEntity.class));
    }

    @Test
    void deleteLabOrder_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(labOrderRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> labOrderService.deleteLabOrder(id));
    }

    // ========== addResult ==========

    @Test
    void addResult_ownerDoctor_persistsResult() {
        UUID orderId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        LabOrderEntity order = buildOrder(orderId, buildPatient(UUID.randomUUID()), doctor);
        LabResultRequest request = buildResultRequest("Glucosa");
        when(labOrderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(order));
        doNothing().when(securityContextService).validateDoctorOwnership(doctorId);

        LabResultDto result = labOrderService.addResult(orderId, request);

        assertNotNull(result);
        assertEquals("Glucosa", result.getTestName());
        verify(labResultRepository).persist(any(LabResultEntity.class));
    }

    @Test
    void addResult_differentDoctor_throwsForbidden() {
        UUID orderId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        LabOrderEntity order = buildOrder(orderId, buildPatient(UUID.randomUUID()), doctor);
        when(labOrderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(order));
        doThrow(new ForbiddenException("No tiene acceso"))
                .when(securityContextService).validateDoctorOwnership(doctorId);

        assertThrows(ForbiddenException.class,
                () -> labOrderService.addResult(orderId, buildResultRequest("Test")));
    }

    @Test
    void addResult_orderNotFound_throws404() {
        UUID orderId = UUID.randomUUID();
        when(labOrderRepository.findByIdOptional(orderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> labOrderService.addResult(orderId, buildResultRequest("Test")));
    }

    // ========== updateResult ==========

    @Test
    void updateResult_ownerDoctor_updatesPartialFields() {
        UUID resultId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        LabOrderEntity order = buildOrder(UUID.randomUUID(), buildPatient(UUID.randomUUID()), doctor);
        LabResultEntity result = buildResultEntity(resultId, order);
        result.setTestName("Hemograma");
        LabResultRequest request = new LabResultRequest();
        request.setTestName("Hemograma completo");
        request.setIsAbnormal(true);
        when(labResultRepository.findByIdOptional(resultId)).thenReturn(Optional.of(result));
        doNothing().when(securityContextService).validateDoctorOwnership(doctorId);

        LabResultDto dto = labOrderService.updateResult(resultId, request);

        assertEquals("Hemograma completo", dto.getTestName());
        assertTrue(dto.getIsAbnormal());
        verify(labResultRepository).persist(result);
    }

    @Test
    void updateResult_resultNotFound_throws404() {
        UUID resultId = UUID.randomUUID();
        when(labResultRepository.findByIdOptional(resultId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> labOrderService.updateResult(resultId, new LabResultRequest()));
    }

    // ========== deleteResult ==========

    @Test
    void deleteResult_ownerDoctor_deletesSuccessfully() {
        UUID resultId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = buildDoctor(doctorId);
        LabOrderEntity order = buildOrder(UUID.randomUUID(), buildPatient(UUID.randomUUID()), doctor);
        LabResultEntity result = buildResultEntity(resultId, order);
        when(labResultRepository.findByIdOptional(resultId)).thenReturn(Optional.of(result));
        doNothing().when(securityContextService).validateDoctorOwnership(doctorId);

        assertDoesNotThrow(() -> labOrderService.deleteResult(resultId));
        verify(labResultRepository).delete(result);
    }

    @Test
    void deleteResult_resultNotFound_throws404() {
        UUID resultId = UUID.randomUUID();
        when(labResultRepository.findByIdOptional(resultId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> labOrderService.deleteResult(resultId));
    }

    // ========== helpers ==========

    private PatientEntity buildPatient(UUID id) {
        PatientEntity p = new PatientEntity();
        p.setId(id);
        p.setFirstName("Pedro");
        p.setLastName("Sánchez");
        p.setBirthdate(LocalDate.of(1978, 7, 22));
        return p;
    }

    private DoctorEntity buildDoctor(UUID id) {
        DoctorEntity d = new DoctorEntity();
        d.setId(id);
        d.setFirstName("Dra. Elena");
        d.setLastName("Vásquez");
        return d;
    }

    private LabOrderEntity buildOrder(UUID id, PatientEntity patient, DoctorEntity doctor) {
        LabOrderEntity o = new LabOrderEntity();
        o.setId(id);
        o.setPatient(patient);
        o.setDoctor(doctor);
        o.setStatus(LabOrderStatus.pending);
        o.setOrderDate(LocalDateTime.now());
        return o;
    }

    private LabResultEntity buildResultEntity(UUID id, LabOrderEntity order) {
        LabResultEntity r = new LabResultEntity();
        r.setId(id);
        r.setLabOrder(order);
        r.setTestName("Colesterol");
        r.setIsAbnormal(false);
        return r;
    }

    private MedicalAppointmentEntity buildAppointment(UUID id, DoctorEntity doctor) {
        MedicalAppointmentEntity a = new MedicalAppointmentEntity();
        a.setId(id);
        a.setDoctor(doctor);
        return a;
    }

    private LabOrderRequest buildOrderRequest(UUID patientId, UUID doctorId) {
        LabOrderRequest r = new LabOrderRequest();
        r.setPatientId(patientId);
        r.setDoctorId(doctorId);
        r.setNotes("Análisis rutinario");
        return r;
    }

    private LabResultRequest buildResultRequest(String testName) {
        LabResultRequest r = new LabResultRequest();
        r.setTestName(testName);
        r.setIsAbnormal(false);
        return r;
    }
}
