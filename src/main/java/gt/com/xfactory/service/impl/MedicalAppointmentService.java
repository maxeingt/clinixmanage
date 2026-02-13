package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.entity.enums.*;
import gt.com.xfactory.repository.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.transaction.*;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

@ApplicationScoped
@Slf4j
public class MedicalAppointmentService {

    @Inject
    PatientRepository patientRepository;

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    ClinicRepository clinicRepository;

    @Inject
    SpecialtyRepository specialtyRepository;

    @Inject
    MedicalAppointmentRepository medicalAppointmentRepository;

    @Inject
    AppointmentDiagnosisRepository appointmentDiagnosisRepository;

    @Inject
    DiagnosisCatalogRepository diagnosisCatalogRepository;

    @Inject
    DoctorSpecialtyRepository doctorSpecialtyRepository;

    @Inject
    SecurityContextService securityContextService;

    private UUID getCurrentDoctorId() {
        return securityContextService.getCurrentDoctorId();
    }

    public List<MedicalAppointmentDto> getMedicalAppointmentsByPatientId(UUID patientId, MedicalAppointmentFilterDto filter) {
        log.info("Fetching medical appointments for patient: {} with filter - doctorId: {}, clinicId: {}",
                patientId, filter != null ? filter.doctorId : null, filter != null ? filter.clinicId : null);

        patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        UUID currentDoctorId = getCurrentDoctorId();
        if (currentDoctorId != null) {
            if (filter == null) filter = new MedicalAppointmentFilterDto();
            filter.doctorId = currentDoctorId;
        }

        return medicalAppointmentRepository.findByPatientIdWithFilters(patientId, filter)
                .stream()
                .map(this::toMedicalAppointmentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicalAppointmentDto createMedicalAppointment(MedicalAppointmentRequest request) {
        log.info("Creating medical appointment for patient: {}", request.getPatientId());

        var patient = patientRepository.findByIdOptional(request.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + request.getPatientId()));

        var doctor = doctorRepository.findByIdOptional(request.getDoctorId())
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + request.getDoctorId()));

        var clinic = clinicRepository.findByIdOptional(request.getClinicId())
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + request.getClinicId()));

        MedicalAppointmentEntity appointment = new MedicalAppointmentEntity();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setClinic(clinic);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setReason(request.getReason());
        appointment.setDiagnosis(request.getDiagnosis());
        appointment.setNotes(request.getNotes());
        appointment.setSource(AppointmentSource.web);

        if (request.getFollowUpAppointmentId() != null) {
            var followUp = medicalAppointmentRepository.findByIdOptional(request.getFollowUpAppointmentId())
                    .orElseThrow(() -> new NotFoundException("Follow-up appointment not found with id: " + request.getFollowUpAppointmentId()));
            appointment.setFollowUpAppointment(followUp);
        }

        if (request.getStatus() != null) {
            applyStatusTransition(appointment, request.getStatus(), request.getCancellationReason());
        } else {
            appointment.setStatus(AppointmentStatus.scheduled);
        }

        if (request.getSpecialtyId() != null) {
            var specialty = specialtyRepository.findByIdOptional(request.getSpecialtyId())
                    .orElseThrow(() -> new NotFoundException("Specialty not found with id: " + request.getSpecialtyId()));
            appointment.setSpecialty(specialty);
        }

        medicalAppointmentRepository.persist(appointment);
        persistDiagnoses(appointment, request.getDiagnoses());
        log.info("Medical appointment created with id: {}", appointment.getId());

        return toMedicalAppointmentDto(appointment);
    }

    @Transactional
    public MedicalAppointmentDto updateMedicalAppointment(UUID appointmentId, MedicalAppointmentRequest request) {
        log.info("Updating medical appointment: {}", appointmentId);

        var appointment = medicalAppointmentRepository.findByIdOptional(appointmentId)
                .orElseThrow(() -> new NotFoundException("Medical appointment not found with id: " + appointmentId));

        validateModifiable(appointment);

        var patient = patientRepository.findByIdOptional(request.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + request.getPatientId()));

        var doctor = doctorRepository.findByIdOptional(request.getDoctorId())
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + request.getDoctorId()));

        var clinic = clinicRepository.findByIdOptional(request.getClinicId())
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + request.getClinicId()));

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setClinic(clinic);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setReason(request.getReason());
        appointment.setDiagnosis(request.getDiagnosis());
        appointment.setNotes(request.getNotes());

        if (request.getFollowUpAppointmentId() != null) {
            var followUp = medicalAppointmentRepository.findByIdOptional(request.getFollowUpAppointmentId())
                    .orElseThrow(() -> new NotFoundException("Follow-up appointment not found with id: " + request.getFollowUpAppointmentId()));
            appointment.setFollowUpAppointment(followUp);
        } else {
            appointment.setFollowUpAppointment(null);
        }

        if (request.getStatus() != null) {
            applyStatusTransition(appointment, request.getStatus(), request.getCancellationReason());
        }

        if (request.getSpecialtyId() != null) {
            var specialty = specialtyRepository.findByIdOptional(request.getSpecialtyId())
                    .orElseThrow(() -> new NotFoundException("Specialty not found with id: " + request.getSpecialtyId()));
            appointment.setSpecialty(specialty);
        } else {
            appointment.setSpecialty(null);
        }

        medicalAppointmentRepository.persist(appointment);
        appointmentDiagnosisRepository.deleteByAppointmentId(appointmentId);
        persistDiagnoses(appointment, request.getDiagnoses());
        log.info("Medical appointment updated: {}", appointmentId);

        return toMedicalAppointmentDto(appointment);
    }

    @Transactional
    public MedicalAppointmentDto reopenMedicalAppointment(UUID appointmentId, ReopenAppointmentRequest request) {
        log.info("Reopening medical appointment: {}", appointmentId);

        var appointment = medicalAppointmentRepository.findByIdOptional(appointmentId)
                .orElseThrow(() -> new NotFoundException("Medical appointment not found with id: " + appointmentId));

        if (appointment.getStatus() != AppointmentStatus.expired) {
            throw new IllegalStateException("Solo se pueden reabrir citas con estado 'expired'. Estado actual: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.reopened);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setNotified30Min(false);
        appointment.setNotified10Min(false);

        medicalAppointmentRepository.persist(appointment);
        log.info("Medical appointment reopened: {}", appointmentId);

        return toMedicalAppointmentDto(appointment);
    }

    @Transactional
    public void deleteMedicalAppointment(UUID appointmentId) {
        log.info("Deleting medical appointment: {}", appointmentId);

        var appointment = medicalAppointmentRepository.findByIdOptional(appointmentId)
                .orElseThrow(() -> new NotFoundException("Medical appointment not found with id: " + appointmentId));

        validateModifiable(appointment);

        appointmentDiagnosisRepository.deleteByAppointmentId(appointmentId);
        medicalAppointmentRepository.delete(appointment);
        log.info("Medical appointment deleted: {}", appointmentId);
    }

    private static final Set<AppointmentStatus> MODIFIABLE_STATUSES = Set.of(
            AppointmentStatus.scheduled,
            AppointmentStatus.confirmed,
            AppointmentStatus.in_progress,
            AppointmentStatus.reopened
    );

    private void applyStatusTransition(MedicalAppointmentEntity appointment, AppointmentStatus newStatus, String cancellationReason) {
        switch (newStatus) {
            case confirmed:
                appointment.setCheckInTime(LocalDateTime.now());
                break;
            case in_progress:
                appointment.setStartTime(LocalDateTime.now());
                break;
            case completed:
                appointment.setEndTime(LocalDateTime.now());
                break;
            case cancelled:
            case no_show:
                appointment.setCancellationReason(cancellationReason);
                break;
            default:
                break;
        }
        appointment.setStatus(newStatus);
    }

    private void persistDiagnoses(MedicalAppointmentEntity appointment, List<AppointmentDiagnosisRequest> diagnoses) {
        if (diagnoses == null || diagnoses.isEmpty()) {
            return;
        }
        for (AppointmentDiagnosisRequest req : diagnoses) {
            var diagCatalog = diagnosisCatalogRepository.findByIdOptional(req.getDiagnosisId())
                    .orElseThrow(() -> new NotFoundException("Diagnosis not found with id: " + req.getDiagnosisId()));

            AppointmentDiagnosisEntity entity = new AppointmentDiagnosisEntity();
            entity.setAppointment(appointment);
            entity.setDiagnosis(diagCatalog);
            entity.setType(req.getType());
            entity.setNotes(req.getNotes());
            appointmentDiagnosisRepository.persist(entity);
        }
    }

    private void validateModifiable(MedicalAppointmentEntity appointment) {
        if (!MODIFIABLE_STATUSES.contains(appointment.getStatus())) {
            throw new IllegalStateException(
                    "No se puede modificar una cita con estado '" + appointment.getStatus() + "'. Solo se permiten: scheduled, confirmed, reopened");
        }
    }

    private UUID resolveSpecialtyId(MedicalAppointmentEntity entity) {
        if (entity.getSpecialty() != null) return entity.getSpecialty().getId();
        return doctorSpecialtyRepository.findByDoctorId(entity.getDoctor().getId())
                .stream().findFirst().map(ds -> ds.getSpecialty().getId()).orElse(null);
    }

    private String resolveSpecialtyName(MedicalAppointmentEntity entity) {
        if (entity.getSpecialty() != null) return entity.getSpecialty().getName();
        return doctorSpecialtyRepository.findByDoctorId(entity.getDoctor().getId())
                .stream().findFirst().map(ds -> ds.getSpecialty().getName()).orElse(null);
    }

    public MedicalAppointmentDto toMedicalAppointmentDto(MedicalAppointmentEntity entity) {
        return MedicalAppointmentDto.builder()
                .id(entity.getId())
                .patientId(entity.getPatient().getId())
                .patientName(entity.getPatient().getFirstName() + " " + entity.getPatient().getLastName())
                .doctorId(entity.getDoctor().getId())
                .doctorName(entity.getDoctor().getFirstName() + " " + entity.getDoctor().getLastName())
                .clinicId(entity.getClinic().getId())
                .clinicName(entity.getClinic().getName())
                .specialtyId(resolveSpecialtyId(entity))
                .specialtyName(resolveSpecialtyName(entity))
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .appointmentDate(entity.getAppointmentDate())
                .reason(entity.getReason())
                .diagnosis(entity.getDiagnosis())
                .notes(entity.getNotes())
                .checkInTime(entity.getCheckInTime())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .cancellationReason(entity.getCancellationReason())
                .source(entity.getSource() != null ? entity.getSource().name() : null)
                .followUpAppointmentId(entity.getFollowUpAppointment() != null ? entity.getFollowUpAppointment().getId() : null)
                .childFollowUpId(medicalAppointmentRepository.findChildFollowUpId(entity.getId()).orElse(null))
                .diagnoses(entity.getDiagnoses() != null ? entity.getDiagnoses().stream()
                        .map(d -> AppointmentDiagnosisDto.builder()
                                .id(d.getId())
                                .diagnosisId(d.getDiagnosis().getId())
                                .code(d.getDiagnosis().getCode())
                                .name(d.getDiagnosis().getName())
                                .type(d.getType() != null ? d.getType().name() : null)
                                .notes(d.getNotes())
                                .build())
                        .collect(Collectors.toList()) : Collections.emptyList())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
