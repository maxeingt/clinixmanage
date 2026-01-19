package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.dto.request.MedicalAppointmentRequest;
import gt.com.xfactory.dto.request.filter.MedicalAppointmentFilterDto;
import gt.com.xfactory.dto.request.filter.PatientFilterDto;
import gt.com.xfactory.dto.response.MedicalAppointmentDto;
import gt.com.xfactory.dto.response.MedicalHistGynecoObstetricDto;
import gt.com.xfactory.dto.response.MedicalHistoryPathologicalFamDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.dto.response.PatientDto;
import gt.com.xfactory.entity.MedicalAppointmentEntity;
import gt.com.xfactory.entity.MedicalHistGynecoObstetricEntity;
import gt.com.xfactory.entity.MedicalHistoryPathologicalFamEntity;
import gt.com.xfactory.entity.PatientEntity;
import gt.com.xfactory.repository.ClinicRepository;
import gt.com.xfactory.repository.DoctorRepository;
import gt.com.xfactory.repository.MedicalAppointmentRepository;
import gt.com.xfactory.repository.MedicalHistGynecoObstetricRepository;
import gt.com.xfactory.repository.MedicalHistoryPathologicalFamRepository;
import gt.com.xfactory.repository.PatientRepository;
import jakarta.transaction.Transactional;
import gt.com.xfactory.utils.QueryUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

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
    MedicalHistGynecoObstetricRepository medicalHistGynecoObstetricRepository;

    @Inject
    MedicalHistoryPathologicalFamRepository medicalHistoryPathologicalFamRepository;

    @Inject
    MedicalAppointmentRepository medicalAppointmentRepository;

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    ClinicRepository clinicRepository;

    public PageResponse<PatientDto> getPatients(PatientFilterDto filter, @Valid CommonPageRequest pageRequest) {
        log.info("Fetching patients with filter - pageRequest: {}, filter: {}", pageRequest, filter);

        StringBuilder query = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();

        QueryUtils.addLikeCondition(filter.name, "name", "name", conditions, params);
        QueryUtils.addLikeCondition(filter.phone, "phone", "phone", conditions, params);
        QueryUtils.addLikeCondition(filter.maritalStatus, "maritalStatus", "maritalStatus", conditions, params);

        if (!conditions.isEmpty()) {
            query.append(String.join(" AND ", conditions));
        }

        return toPageResponse(patientRepository, query, pageRequest, params, toDto);
    }

    public PatientDto getPatientById(UUID patientId) {
        log.info("Fetching patient by id: {}", patientId);
        return patientRepository.findByIdOptional(patientId)
                .map(toDto)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));
    }

    public List<MedicalHistGynecoObstetricDto> getMedicalHistGynecoObstetricByPatientId(UUID patientId) {
        log.info("Fetching medical hist gyneco obstetric for patient: {}", patientId);

        // Verify patient exists
        patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        return medicalHistGynecoObstetricRepository.findByPatientId(patientId)
                .stream()
                .map(toMedicalHistGynecoDto)
                .collect(Collectors.toList());
    }

    public List<MedicalHistoryPathologicalFamDto> getMedicalHistoryPathologicalFamByPatientId(UUID patientId) {
        log.info("Fetching medical history pathological fam for patient: {}", patientId);

        // Verify patient exists
        patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        return medicalHistoryPathologicalFamRepository.findByPatientId(patientId)
                .stream()
                .map(toMedicalHistoryPathologicalFamDto)
                .collect(Collectors.toList());
    }

    // Medical Appointment CRUD operations

    public List<MedicalAppointmentDto> getMedicalAppointmentsByPatientId(UUID patientId, MedicalAppointmentFilterDto filter) {
        log.info("Fetching medical appointments for patient: {} with filter - doctorId: {}, clinicId: {}",
                patientId, filter != null ? filter.doctorId : null, filter != null ? filter.clinicId : null);

        // Verify patient exists
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
        appointment.setObservation(request.getObservation());
        appointment.setMedicalHistory(request.getMedicalHistory());

        if (request.getMedHistGynecoId() != null) {
            var medHistGyneco = medicalHistGynecoObstetricRepository.findByIdOptional(request.getMedHistGynecoId())
                    .orElseThrow(() -> new NotFoundException("Medical hist gyneco not found with id: " + request.getMedHistGynecoId()));
            appointment.setMedHistGyneco(medHistGyneco);
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
        appointment.setObservation(request.getObservation());
        appointment.setMedicalHistory(request.getMedicalHistory());

        if (request.getMedHistGynecoId() != null) {
            var medHistGyneco = medicalHistGynecoObstetricRepository.findByIdOptional(request.getMedHistGynecoId())
                    .orElseThrow(() -> new NotFoundException("Medical hist gyneco not found with id: " + request.getMedHistGynecoId()));
            appointment.setMedHistGyneco(medHistGyneco);
        } else {
            appointment.setMedHistGyneco(null);
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

    public static final Function<PatientEntity, PatientDto> toDto = entity ->
            PatientDto.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .age(entity.getAge())
                    .phone(entity.getPhone())
                    .address(entity.getAddress())
                    .maritalStatus(entity.getMaritalStatus())
                    .occupation(entity.getOccupation())
                    .build();

    public static final Function<MedicalHistGynecoObstetricEntity, MedicalHistGynecoObstetricDto> toMedicalHistGynecoDto = entity ->
            MedicalHistGynecoObstetricDto.builder()
                    .id(entity.getId())
                    .patientId(entity.getPatient().getId())
                    .medicalHistoryType(entity.getMedicalHistoryType())
                    .lastMenstrualPeriod(entity.getLastMenstrualPeriod())
                    .weight(entity.getWeight())
                    .height(entity.getHeight())
                    .duration(entity.getDuration())
                    .cycles(entity.getCycles())
                    .reliable(entity.getReliable())
                    .papanicolaou(entity.getPapanicolaou())
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
                    .doctorId(entity.getDoctor().getId())
                    .clinicId(entity.getClinic().getId())
                    .medHistGynecoId(entity.getMedHistGyneco() != null ? entity.getMedHistGyneco().getId() : null)
                    .appointmentDate(entity.getAppointmentDate())
                    .observation(entity.getObservation())
                    .medicalHistory(entity.getMedicalHistory())
                    .build();
}
