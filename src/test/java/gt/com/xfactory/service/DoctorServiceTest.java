package gt.com.xfactory.service;

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
class DoctorServiceTest {

    @InjectMock
    DoctorRepository doctorRepository;

    @InjectMock
    ClinicRepository clinicRepository;

    @InjectMock
    DoctorClinicRepository doctorClinicRepository;

    @InjectMock
    DoctorSpecialtyRepository doctorSpecialtyRepository;

    @InjectMock
    SpecialtyRepository specialtyRepository;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    MedicalAppointmentRepository medicalAppointmentRepository;

    @Inject
    DoctorService doctorService;

    // ========== addClinicToDoctor ==========

    @Test
    void addClinicToDoctor_newRelation_createsEntity() {
        UUID doctorId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();
        DoctorEntity doctor = doctorWith(doctorId);
        ClinicEntity clinic = clinicWith(clinicId, "Clínica Central");
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(doctor));
        when(clinicRepository.findByIdOptional(clinicId)).thenReturn(Optional.of(clinic));
        when(doctorClinicRepository.findByDoctorIdAndClinicId(doctorId, clinicId)).thenReturn(Optional.empty());

        ClinicDto result = doctorService.addClinicToDoctor(doctorId, clinicId);

        assertEquals(clinicId, result.getId());
        verify(doctorClinicRepository).persist(any(DoctorClinicEntity.class));
    }

    @Test
    void addClinicToDoctor_inactiveExistingRelation_reactivates() {
        UUID doctorId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();
        DoctorEntity doctor = doctorWith(doctorId);
        ClinicEntity clinic = clinicWith(clinicId, "Clínica Sur");
        DoctorClinicEntity existing = buildDoctorClinic(doctorId, clinicId, false);
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(doctor));
        when(clinicRepository.findByIdOptional(clinicId)).thenReturn(Optional.of(clinic));
        when(doctorClinicRepository.findByDoctorIdAndClinicId(doctorId, clinicId)).thenReturn(Optional.of(existing));

        doctorService.addClinicToDoctor(doctorId, clinicId);

        assertTrue(existing.getActive(), "Debe reactivar la relación");
        assertNull(existing.getUnassignedAt(), "Debe limpiar unassignedAt");
        assertNotNull(existing.getAssignedAt(), "Debe actualizar assignedAt");
        verify(doctorClinicRepository).persist(existing);
    }

    @Test
    void addClinicToDoctor_activeExistingRelation_doesNotDuplicate() {
        UUID doctorId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();
        DoctorEntity doctor = doctorWith(doctorId);
        ClinicEntity clinic = clinicWith(clinicId, "Clínica Norte");
        DoctorClinicEntity existing = buildDoctorClinic(doctorId, clinicId, true);
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(doctor));
        when(clinicRepository.findByIdOptional(clinicId)).thenReturn(Optional.of(clinic));
        when(doctorClinicRepository.findByDoctorIdAndClinicId(doctorId, clinicId)).thenReturn(Optional.of(existing));

        doctorService.addClinicToDoctor(doctorId, clinicId);

        // No debe persistir un nuevo registro
        verify(doctorClinicRepository, never()).persist(any(DoctorClinicEntity.class));
    }

    // ========== removeClinicFromDoctor ==========

    @Test
    void removeClinicFromDoctor_softDeletes() {
        UUID doctorId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();
        DoctorClinicEntity existing = buildDoctorClinic(doctorId, clinicId, true);
        when(doctorClinicRepository.findByDoctorIdAndClinicId(doctorId, clinicId)).thenReturn(Optional.of(existing));

        doctorService.removeClinicFromDoctor(doctorId, clinicId);

        assertFalse(existing.getActive(), "Debe marcar como inactivo");
        assertNotNull(existing.getUnassignedAt(), "Debe registrar la fecha de desvinculación");
        verify(doctorClinicRepository).persist(existing);
    }

    @Test
    void removeClinicFromDoctor_notFound_throwsNotFoundException() {
        UUID doctorId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();
        when(doctorClinicRepository.findByDoctorIdAndClinicId(doctorId, clinicId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> doctorService.removeClinicFromDoctor(doctorId, clinicId));
    }

    // ========== deleteDoctor ==========

    @Test
    void deleteDoctor_noAppointments_deletesSuccessfully() {
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = doctorWith(doctorId);
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(doctor));
        // medicalAppointmentRepository.count() retorna 0L por defecto (Mockito default)

        assertDoesNotThrow(() -> doctorService.deleteDoctor(doctorId));
        verify(doctorSpecialtyRepository).deleteByDoctorId(doctorId);
        verify(doctorRepository).delete(doctor);
    }

    @Test
    void deleteDoctor_withAppointments_throwsIllegalState() {
        UUID doctorId = UUID.randomUUID();
        DoctorEntity doctor = doctorWith(doctorId);
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(doctor));
        // doReturn evita ambigüedad con varargs Object... en count()
        doReturn(3L).when(medicalAppointmentRepository).count(anyString(), any(Object[].class));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> doctorService.deleteDoctor(doctorId));

        assertTrue(ex.getMessage().contains("citas médicas"));
        verify(doctorRepository, never()).delete(any(DoctorEntity.class));
    }

    @Test
    void deleteDoctor_notFound_throwsNotFoundException() {
        UUID doctorId = UUID.randomUUID();
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> doctorService.deleteDoctor(doctorId));
    }

    // ========== addSpecialtyToDoctor ==========

    @Test
    void addSpecialtyToDoctor_newSpecialty_persists() {
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();
        DoctorEntity doctor = doctorWith(doctorId);
        SpecialtyEntity specialty = specialtyWith(specialtyId, "Pediatría");
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(doctor));
        when(specialtyRepository.findByIdOptional(specialtyId)).thenReturn(Optional.of(specialty));
        when(doctorSpecialtyRepository.findByDoctorIdAndSpecialtyId(doctorId, specialtyId))
                .thenReturn(Optional.empty());

        SpecialtyDto result = doctorService.addSpecialtyToDoctor(doctorId, specialtyId);

        assertEquals(specialtyId, result.getId());
        assertEquals("Pediatría", result.getName());
        verify(doctorSpecialtyRepository).persist(any(DoctorSpecialtyEntity.class));
    }

    @Test
    void addSpecialtyToDoctor_duplicateSpecialty_doesNotPersist() {
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();
        DoctorEntity doctor = doctorWith(doctorId);
        SpecialtyEntity specialty = specialtyWith(specialtyId, "Cardiología");
        DoctorSpecialtyEntity existingRelation = new DoctorSpecialtyEntity();
        when(doctorRepository.findByIdOptional(doctorId)).thenReturn(Optional.of(doctor));
        when(specialtyRepository.findByIdOptional(specialtyId)).thenReturn(Optional.of(specialty));
        when(doctorSpecialtyRepository.findByDoctorIdAndSpecialtyId(doctorId, specialtyId))
                .thenReturn(Optional.of(existingRelation));

        SpecialtyDto result = doctorService.addSpecialtyToDoctor(doctorId, specialtyId);

        assertEquals(specialtyId, result.getId());
        // No debe persistir si ya existe
        verify(doctorSpecialtyRepository, never()).persist(any(DoctorSpecialtyEntity.class));
    }

    // ========== helpers ==========

    private DoctorEntity doctorWith(UUID id) {
        DoctorEntity e = new DoctorEntity();
        e.setId(id);
        e.setFirstName("Carlos");
        e.setLastName("Médico");
        return e;
    }

    private ClinicEntity clinicWith(UUID id, String name) {
        ClinicEntity e = new ClinicEntity();
        e.setId(id);
        e.setName(name);
        e.setAddress("Dirección test");
        e.setPhone("50001234");
        return e;
    }

    private DoctorClinicEntity buildDoctorClinic(UUID doctorId, UUID clinicId, boolean active) {
        DoctorEntity doctor = doctorWith(doctorId);
        ClinicEntity clinic = clinicWith(clinicId, "Clínica");
        DoctorClinicEntity dc = new DoctorClinicEntity();
        dc.setId(new DoctorClinicId(doctorId, clinicId));
        dc.setDoctor(doctor);
        dc.setClinic(clinic);
        dc.setActive(active);
        dc.setAssignedAt(LocalDateTime.now().minusDays(10));
        if (!active) {
            dc.setUnassignedAt(LocalDateTime.now().minusDays(5));
        }
        return dc;
    }

    private SpecialtyEntity specialtyWith(UUID id, String name) {
        SpecialtyEntity e = new SpecialtyEntity();
        e.setId(id);
        e.setName(name);
        e.setDescription("Especialidad de prueba");
        return e;
    }
}
