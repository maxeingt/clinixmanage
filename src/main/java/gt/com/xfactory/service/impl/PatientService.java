package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.entity.converter.GenderTypeConverter;
import gt.com.xfactory.entity.enums.*;
import gt.com.xfactory.repository.*;
import gt.com.xfactory.utils.*;
import io.quarkus.security.identity.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.transaction.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;
import org.apache.commons.lang3.*;
import org.eclipse.microprofile.jwt.*;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static gt.com.xfactory.dto.response.PageResponse.toPageResponse;

@ApplicationScoped
@Slf4j
public class PatientService {

    @Inject
    PatientRepository patientRepository;

    @Inject
    MedicalHistoryPathologicalFamRepository medicalHistoryPathologicalFamRepository;

    @Inject
    MedicalAppointmentRepository medicalAppointmentRepository;

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    ClinicRepository clinicRepository;

    @Inject
    MedicalRecordRepository medicalRecordRepository;

    @Inject
    PrescriptionRepository prescriptionRepository;

    @Inject
    SpecialtyRepository specialtyRepository;

    @Inject
    AppointmentDiagnosisRepository appointmentDiagnosisRepository;

    @Inject
    DiagnosisCatalogRepository diagnosisCatalogRepository;

    @Inject
    JsonWebToken jwt;

    @Inject
    SecurityIdentity securityIdentity;

    private UUID getCurrentDoctorId() {
        if (securityIdentity.hasRole("admin") || securityIdentity.hasRole("secretary")) {
            return null;
        }
        String keycloakId = jwt.getSubject();
        return doctorRepository.findByUserKeycloakId(keycloakId)
                .map(DoctorEntity::getId)
                .orElseThrow(() -> new ForbiddenException("Doctor no encontrado para el usuario actual"));
    }

    public PageResponse<PatientDto> getPatients(PatientFilterDto filter, @Valid CommonPageRequest pageRequest) {
        log.info("Fetching patients with filter - pageRequest: {}, filter: {}", pageRequest, filter);

        StringBuilder query = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();

        UUID currentDoctorId = getCurrentDoctorId();
        if (currentDoctorId != null) {
            conditions.add("id IN (SELECT DISTINCT a.patient.id FROM MedicalAppointmentEntity a WHERE a.doctor.id = :currentDoctorId)");
            params.put("currentDoctorId", currentDoctorId);
        }

        if (StringUtils.isNotBlank(filter.name)) {
            conditions.add("(LOWER(firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(lastName) LIKE LOWER(CONCAT('%', :name, '%')))");
            params.put("name", filter.name);
        }
        QueryUtils.addLikeCondition(filter.phone, "phone", "phone", conditions, params);
        QueryUtils.addLikeCondition(filter.maritalStatus, "maritalStatus", "maritalStatus", conditions, params);

        if (!conditions.isEmpty()) {
            query.append(String.join(" AND ", conditions));
        }

        return toPageResponse(patientRepository, query, pageRequest, params, toDto);
    }

    @Transactional
    public PatientDto createPatient(PatientRequest request) {
        log.info("Creating patient: {} {}", request.getFirstName(), request.getLastName());

        PatientEntity patient = new PatientEntity();
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setBirthdate(request.getBirthdate());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setAddress(request.getAddress());
        patient.setMaritalStatus(request.getMaritalStatus() != null ? request.getMaritalStatus() : "Soltero");
        patient.setOccupation(request.getOccupation());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setAllergies(request.getAllergies());
        patient.setChronicConditions(request.getChronicConditions());
        patient.setInsuranceProvider(request.getInsuranceProvider());
        patient.setInsuranceNumber(request.getInsuranceNumber());
        patient.setDpi(StringUtils.isNotBlank(request.getDpi()) ? request.getDpi() : null);
        patient.setNationality(request.getNationality());
        patient.setHeight(request.getHeight());
        patient.setWeight(request.getWeight());

        if (request.getGender() != null && !request.getGender().isBlank()) {
            patient.setGender(GenderType.fromValue(request.getGender()));
        }

        if (request.getBloodGroup() != null && !request.getBloodGroup().isBlank()) {
            patient.setBloodGroup(BloodType.fromValue(request.getBloodGroup()));
        }

        patientRepository.persist(patient);
        log.info("Patient created with id: {}", patient.getId());

        return toDto.apply(patient);
    }

    @Transactional
    public PatientDto updatePatient(UUID patientId, PatientRequest request) {
        log.info("Updating patient: {}", patientId);

        PatientEntity patient = patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setBirthdate(request.getBirthdate());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setAddress(request.getAddress());
        patient.setMaritalStatus(request.getMaritalStatus() != null ? request.getMaritalStatus() : "Soltero");
        patient.setOccupation(request.getOccupation());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setAllergies(request.getAllergies());
        patient.setChronicConditions(request.getChronicConditions());
        patient.setInsuranceProvider(request.getInsuranceProvider());
        patient.setInsuranceNumber(request.getInsuranceNumber());
        patient.setDpi(StringUtils.isNotBlank(request.getDpi()) ? request.getDpi() : null);
        patient.setNationality(request.getNationality());
        patient.setHeight(request.getHeight());
        patient.setWeight(request.getWeight());

        if (request.getGender() != null && !request.getGender().isBlank()) {
            patient.setGender(GenderType.fromValue(request.getGender()));
        }

        if (request.getBloodGroup() != null && !request.getBloodGroup().isBlank()) {
            patient.setBloodGroup(BloodType.fromValue(request.getBloodGroup()));
        }

        patientRepository.persist(patient);
        log.info("Patient updated: {}", patientId);

        return toDto.apply(patient);
    }

    public PatientDto getPatientById(UUID patientId) {
        log.info("Fetching patient by id: {}", patientId);

        UUID currentDoctorId = getCurrentDoctorId();
        if (currentDoctorId != null) {
            long count = medicalAppointmentRepository.count("patient.id = ?1 AND doctor.id = ?2", patientId, currentDoctorId);
            if (count == 0) {
                throw new ForbiddenException("No tiene acceso a este paciente");
            }
        }

        return patientRepository.findByIdOptional(patientId)
                .map(toDto)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));
    }

    public List<MedicalHistoryPathologicalFamDto> getMedicalHistoryPathologicalFamByPatientId(UUID patientId) {
        log.info("Fetching medical history pathological fam for patient: {}", patientId);

        patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        UUID currentDoctorId = getCurrentDoctorId();
        if (currentDoctorId != null) {
            long count = medicalAppointmentRepository.count(
                    "patient.id = ?1 AND doctor.id = ?2", patientId, currentDoctorId);
            if (count == 0) {
                throw new ForbiddenException("No tiene acceso a este paciente");
            }
        }

        return medicalHistoryPathologicalFamRepository.findByPatientId(patientId)
                .stream()
                .map(toMedicalHistoryPathologicalFamDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicalHistoryPathologicalFamDto createMedicalHistoryPathologicalFam(MedicalHistoryPathologicalFamRequest request) {
        log.info("Creating medical history pathological fam for patient: {}", request.getPatientId());

        var patient = patientRepository.findByIdOptional(request.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + request.getPatientId()));

        UUID currentDoctorId = getCurrentDoctorId();
        if (currentDoctorId != null) {
            long count = medicalAppointmentRepository.count(
                    "patient.id = ?1 AND doctor.id = ?2", request.getPatientId(), currentDoctorId);
            if (count == 0) {
                throw new ForbiddenException("No tiene acceso a este paciente");
            }
        }

        MedicalHistoryPathologicalFamEntity entity = new MedicalHistoryPathologicalFamEntity();
        entity.setPatient(patient);
        entity.setMedicalHistoryType(request.getMedicalHistoryType());
        entity.setDescription(request.getDescription());

        medicalHistoryPathologicalFamRepository.persist(entity);
        log.info("Medical history pathological fam created with id: {}", entity.getId());

        // Actualizar bandera del paciente
        if (!Boolean.TRUE.equals(patient.getHasPathologicalHistory())) {
            patient.setHasPathologicalHistory(true);
            patientRepository.persist(patient);
            log.info("Patient {} marked as having pathological history", patient.getId());
        }

        return toMedicalHistoryPathologicalFamDto.apply(entity);
    }

    @Transactional
    public MedicalHistoryPathologicalFamDto updateMedicalHistoryPathologicalFam(UUID historyId, MedicalHistoryPathologicalFamRequest request) {
        log.info("Updating medical history pathological fam: {}", historyId);

        var entity = medicalHistoryPathologicalFamRepository.find("id", historyId).firstResultOptional()
                .orElseThrow(() -> new NotFoundException("Medical history not found with id: " + historyId));

        UUID currentDoctorId = getCurrentDoctorId();
        if (currentDoctorId != null) {
            UUID patientId = entity.getPatient().getId();
            long count = medicalAppointmentRepository.count(
                    "patient.id = ?1 AND doctor.id = ?2", patientId, currentDoctorId);
            if (count == 0) {
                throw new ForbiddenException("No tiene acceso a este paciente");
            }
        }

        entity.setMedicalHistoryType(request.getMedicalHistoryType());
        entity.setDescription(request.getDescription());

        medicalHistoryPathologicalFamRepository.persist(entity);
        log.info("Medical history pathological fam updated: {}", historyId);

        return toMedicalHistoryPathologicalFamDto.apply(entity);
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

    @Transactional
    public void deletePatient(UUID patientId) {
        log.info("Deleting patient: {}", patientId);

        PatientEntity patient = patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        List<String> relatedData = new ArrayList<>();

        if (medicalAppointmentRepository.count("patient.id", patientId) > 0) {
            relatedData.add("citas médicas");
        }
        if (medicalHistoryPathologicalFamRepository.count("patient.id", patientId) > 0) {
            relatedData.add("historial médico patológico/familiar");
        }
        if (medicalRecordRepository.count("patient.id", patientId) > 0) {
            relatedData.add("expedientes médicos");
        }
        if (prescriptionRepository.count("patient.id", patientId) > 0) {
            relatedData.add("recetas");
        }

        if (!relatedData.isEmpty()) {
            throw new IllegalStateException(
                    "No se puede eliminar el paciente porque tiene datos relacionados: " + String.join(", ", relatedData));
        }

        patientRepository.delete(patient);
        log.info("Patient deleted: {}", patientId);
    }

    private int calculateAge(LocalDate birthdate) {
        if (birthdate == null) {
            return 0;
        }
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

    public static final Function<PatientEntity, PatientDto> toDto = entity ->
            PatientDto.builder()
                    .id(entity.getId())
                    .firstName(entity.getFirstName())
                    .lastName(entity.getLastName())
                    .birthdate(entity.getBirthdate())
                    .age(entity.getBirthdate() != null ? Period.between(entity.getBirthdate(), LocalDate.now()).getYears() : 0)
                    .gender(entity.getGender() != null ? entity.getGender().name() : null)
                    .bloodGroup(entity.getBloodGroup() != null ? entity.getBloodGroup().name() : null)
                    .phone(entity.getPhone())
                    .email(entity.getEmail())
                    .address(entity.getAddress())
                    .maritalStatus(entity.getMaritalStatus())
                    .occupation(entity.getOccupation())
                    .emergencyContactName(entity.getEmergencyContactName())
                    .emergencyContactPhone(entity.getEmergencyContactPhone())
                    .allergies(entity.getAllergies())
                    .chronicConditions(entity.getChronicConditions())
                    .insuranceProvider(entity.getInsuranceProvider())
                    .insuranceNumber(entity.getInsuranceNumber())
                    .dpi(entity.getDpi())
                    .nationality(entity.getNationality())
                    .height(entity.getHeight())
                    .weight(entity.getWeight())
                    .hasPathologicalHistory(entity.getHasPathologicalHistory())
                    .build();

    public static final Function<MedicalHistoryPathologicalFamEntity, MedicalHistoryPathologicalFamDto> toMedicalHistoryPathologicalFamDto = entity ->
            MedicalHistoryPathologicalFamDto.builder()
                    .id(entity.getId())
                    .patientId(entity.getPatient().getId())
                    .medicalHistoryType(entity.getMedicalHistoryType())
                    .description(entity.getDescription())
                    .build();

    private static final java.util.Set<AppointmentStatus> MODIFIABLE_STATUSES = java.util.Set.of(
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

    public MedicalAppointmentDto toMedicalAppointmentDto(MedicalAppointmentEntity entity) {
        return MedicalAppointmentDto.builder()
                .id(entity.getId())
                .patientId(entity.getPatient().getId())
                .patientName(entity.getPatient().getFirstName() + " " + entity.getPatient().getLastName())
                .doctorId(entity.getDoctor().getId())
                .doctorName(entity.getDoctor().getFirstName() + " " + entity.getDoctor().getLastName())
                .clinicId(entity.getClinic().getId())
                .clinicName(entity.getClinic().getName())
                .specialtyId(entity.getSpecialty() != null ? entity.getSpecialty().getId() : null)
                .specialtyName(entity.getSpecialty() != null ? entity.getSpecialty().getName() : null)
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
