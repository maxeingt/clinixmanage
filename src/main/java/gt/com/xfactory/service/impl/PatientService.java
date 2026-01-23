package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.dto.request.MedicalAppointmentRequest;
import gt.com.xfactory.dto.request.MedicalHistoryPathologicalFamRequest;
import gt.com.xfactory.dto.request.PatientRequest;
import gt.com.xfactory.dto.request.filter.MedicalAppointmentFilterDto;
import gt.com.xfactory.dto.request.filter.PatientFilterDto;
import gt.com.xfactory.dto.response.MedicalAppointmentDto;
import gt.com.xfactory.dto.response.MedicalHistoryPathologicalFamDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.dto.response.PatientDto;
import gt.com.xfactory.entity.MedicalAppointmentEntity;
import gt.com.xfactory.entity.MedicalHistoryPathologicalFamEntity;
import gt.com.xfactory.entity.PatientEntity;
import gt.com.xfactory.entity.enums.AppointmentStatus;
import gt.com.xfactory.entity.enums.BloodType;
import gt.com.xfactory.entity.enums.GenderType;
import gt.com.xfactory.repository.ClinicRepository;
import gt.com.xfactory.repository.DoctorRepository;
import gt.com.xfactory.repository.MedicalAppointmentRepository;
import gt.com.xfactory.repository.MedicalHistoryPathologicalFamRepository;
import gt.com.xfactory.repository.PatientRepository;
import gt.com.xfactory.repository.SpecialtyRepository;
import jakarta.transaction.Transactional;
import gt.com.xfactory.utils.QueryUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    SpecialtyRepository specialtyRepository;

    public PageResponse<PatientDto> getPatients(PatientFilterDto filter, @Valid CommonPageRequest pageRequest) {
        log.info("Fetching patients with filter - pageRequest: {}, filter: {}", pageRequest, filter);

        StringBuilder query = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();

        QueryUtils.addLikeCondition(filter.name, "firstName", "firstName", conditions, params);
        QueryUtils.addLikeCondition(filter.name, "lastName", "lastName", conditions, params);
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

        if (request.getGender() != null && !request.getGender().isBlank()) {
            try {
                patient.setGender(GenderType.valueOf(request.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid gender value: {}", request.getGender());
            }
        }

        if (request.getBloodGroup() != null && !request.getBloodGroup().isBlank()) {
            patient.setBloodGroup(BloodType.fromValue(request.getBloodGroup()));
        }

        patientRepository.persist(patient);
        log.info("Patient created with id: {}", patient.getId());

        return toDto.apply(patient);
    }

    public PatientDto getPatientById(UUID patientId) {
        log.info("Fetching patient by id: {}", patientId);
        return patientRepository.findByIdOptional(patientId)
                .map(toDto)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));
    }

    public List<MedicalHistoryPathologicalFamDto> getMedicalHistoryPathologicalFamByPatientId(UUID patientId) {
        log.info("Fetching medical history pathological fam for patient: {}", patientId);

        patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

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

        return medicalAppointmentRepository.findByPatientIdWithFilters(patientId, filter)
                .stream()
                .map(toMedicalAppointmentDto)
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
        appointment.setStatus(request.getStatus() != null ? request.getStatus() : AppointmentStatus.scheduled);

        if (request.getSpecialtyId() != null) {
            var specialty = specialtyRepository.findByIdOptional(request.getSpecialtyId())
                    .orElseThrow(() -> new NotFoundException("Specialty not found with id: " + request.getSpecialtyId()));
            appointment.setSpecialty(specialty);
        }

        medicalAppointmentRepository.persist(appointment);
        log.info("Medical appointment created with id: {}", appointment.getId());

        return toMedicalAppointmentDto.apply(appointment);
    }

    @Transactional
    public MedicalAppointmentDto updateMedicalAppointment(UUID appointmentId, MedicalAppointmentRequest request) {
        log.info("Updating medical appointment: {}", appointmentId);

        var appointment = medicalAppointmentRepository.findByIdOptional(appointmentId)
                .orElseThrow(() -> new NotFoundException("Medical appointment not found with id: " + appointmentId));

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

        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }

        if (request.getSpecialtyId() != null) {
            var specialty = specialtyRepository.findByIdOptional(request.getSpecialtyId())
                    .orElseThrow(() -> new NotFoundException("Specialty not found with id: " + request.getSpecialtyId()));
            appointment.setSpecialty(specialty);
        } else {
            appointment.setSpecialty(null);
        }

        medicalAppointmentRepository.persist(appointment);
        log.info("Medical appointment updated: {}", appointmentId);

        return toMedicalAppointmentDto.apply(appointment);
    }

    @Transactional
    public void deleteMedicalAppointment(UUID appointmentId) {
        log.info("Deleting medical appointment: {}", appointmentId);

        var appointment = medicalAppointmentRepository.findByIdOptional(appointmentId)
                .orElseThrow(() -> new NotFoundException("Medical appointment not found with id: " + appointmentId));

        medicalAppointmentRepository.delete(appointment);
        log.info("Medical appointment deleted: {}", appointmentId);
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
                    .hasPathologicalHistory(entity.getHasPathologicalHistory())
                    .build();

    public static final Function<MedicalHistoryPathologicalFamEntity, MedicalHistoryPathologicalFamDto> toMedicalHistoryPathologicalFamDto = entity ->
            MedicalHistoryPathologicalFamDto.builder()
                    .id(entity.getId())
                    .patientId(entity.getPatient().getId())
                    .medicalHistoryType(entity.getMedicalHistoryType())
                    .description(entity.getDescription())
                    .build();

    public static final Function<MedicalAppointmentEntity, MedicalAppointmentDto> toMedicalAppointmentDto = entity ->
            MedicalAppointmentDto.builder()
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
                    .createdAt(entity.getCreatedAt())
                    .build();
}
