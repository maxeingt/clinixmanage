package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.entity.enums.*;
import gt.com.xfactory.repository.*;
import gt.com.xfactory.utils.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.transaction.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;
import org.apache.commons.lang3.*;

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
    MedicalRecordRepository medicalRecordRepository;

    @Inject
    PrescriptionRepository prescriptionRepository;

    @Inject
    SecurityContextService securityContextService;

    private UUID getCurrentDoctorId() {
        return securityContextService.getCurrentDoctorId();
    }

    public PageResponse<PatientDto> getPatients(PatientFilterDto filter, @Valid CommonPageRequest pageRequest) {
        log.info("Fetching patients with filter - pageRequest: {}, filter: {}", pageRequest, filter);

        UUID currentDoctorId = getCurrentDoctorId();
        var fb = FilterBuilder.create()
                .addCondition(currentDoctorId != null,
                        "id IN (SELECT DISTINCT a.patient.id FROM MedicalAppointmentEntity a WHERE a.doctor.id = :currentDoctorId)",
                        "currentDoctorId", currentDoctorId)
                .addCondition(StringUtils.isNotBlank(filter.name),
                        "(LOWER(firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(lastName) LIKE LOWER(CONCAT('%', :name, '%')))",
                        "name", filter.name)
                .addLike(filter.phone, "phone")
                .addLike(filter.maritalStatus, "maritalStatus");

        return toPageResponse(patientRepository, fb.buildQuery(), pageRequest, fb.getParams(), toDto);
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

}
