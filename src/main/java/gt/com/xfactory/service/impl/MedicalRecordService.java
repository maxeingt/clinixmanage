package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
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
public class MedicalRecordService {

    @Inject
    MedicalRecordRepository medicalRecordRepository;

    @Inject
    PatientRepository patientRepository;

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    MedicalAppointmentRepository medicalAppointmentRepository;

    @Inject
    SpecialtyRepository specialtyRepository;

    @Inject
    SpecialtyFormTemplateRepository specialtyFormTemplateRepository;

    @Inject
    SecurityContextService securityContextService;

    private UUID getCurrentDoctorId() {
        return securityContextService.getCurrentDoctorId();
    }

    public PageResponse<MedicalRecordDto> getMedicalRecordsPaginated(MedicalRecordFilterDto filter, @Valid CommonPageRequest pageRequest) {
        log.info("Fetching medical records with filter - pageRequest: {}, filter: {}", pageRequest, filter);

        UUID currentDoctorId = getCurrentDoctorId();
        var fb = FilterBuilder.create()
                .addEquals(currentDoctorId, "doctor.id", "currentDoctorId")
                .addEquals(filter.patientId, "patient.id", "patientId")
                .addEquals(filter.doctorId, "doctor.id", "doctorId")
                .addEquals(filter.specialtyId, "specialty.id", "specialtyId")
                .addCondition(StringUtils.isNotBlank(filter.recordType),
                        "recordType = :recordType", "recordType",
                        StringUtils.isNotBlank(filter.recordType) ? MedicalRecordType.valueOf(filter.recordType) : null)
                .addDateRange(filter.startDate, "createdAt", "startDate",
                              filter.endDate, "createdAt", "endDate");

        return toPageResponse(medicalRecordRepository, fb.buildQuery(), pageRequest, fb.getParams(), toMedicalRecordDto);
    }

    public List<MedicalRecordDto> getMedicalRecordsByPatientId(UUID patientId) {
        log.info("Fetching medical records for patient: {}", patientId);

        patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        UUID currentDoctorId = getCurrentDoctorId();
        List<MedicalRecordEntity> records = currentDoctorId != null
                ? medicalRecordRepository.findByPatientIdAndDoctorId(patientId, currentDoctorId)
                : medicalRecordRepository.findByPatientId(patientId);
        return records.stream().map(toMedicalRecordDto).collect(Collectors.toList());
    }

    public List<MedicalRecordDto> getMedicalRecordsByAppointmentId(UUID appointmentId) {
        log.info("Fetching medical records for appointment: {}", appointmentId);

        MedicalAppointmentEntity appointment = medicalAppointmentRepository.findByIdOptional(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found with id: " + appointmentId));

        UUID currentDoctorId = getCurrentDoctorId();
        if (currentDoctorId != null && !appointment.getDoctor().getId().equals(currentDoctorId)) {
            throw new ForbiddenException("No tiene acceso a los expedientes de esta cita");
        }

        return medicalRecordRepository.findByAppointmentId(appointmentId).stream()
                .map(toMedicalRecordDto).collect(Collectors.toList());
    }

    public MedicalRecordDto getMedicalRecordById(UUID recordId) {
        log.info("Fetching medical record by id: {}", recordId);

        MedicalRecordEntity record = medicalRecordRepository.findByIdOptional(recordId)
                .orElseThrow(() -> new NotFoundException("Medical record not found with id: " + recordId));

        UUID currentDoctorId = getCurrentDoctorId();
        if (currentDoctorId != null && !record.getDoctor().getId().equals(currentDoctorId)) {
            throw new ForbiddenException("No tiene acceso a este expediente");
        }

        return toMedicalRecordDto.apply(record);
    }

    @Transactional
    public MedicalRecordDto createMedicalRecord(MedicalRecordRequest request) {
        log.info("Creating medical record for patient: {}", request.getPatientId());

        var patient = patientRepository.findByIdOptional(request.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + request.getPatientId()));

        var doctor = doctorRepository.findByIdOptional(request.getDoctorId())
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + request.getDoctorId()));

        MedicalRecordEntity record = new MedicalRecordEntity();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setRecordType(request.getRecordType() != null ? request.getRecordType() : MedicalRecordType.consultation);
        record.setChiefComplaint(request.getChiefComplaint());
        record.setPresentIllness(request.getPresentIllness());
        record.setPhysicalExam(request.getPhysicalExam());
        record.setTreatmentPlan(request.getTreatmentPlan());
        record.setVitalSigns(request.getVitalSigns());
        record.setSpecialtyData(request.getSpecialtyData());
        record.setAttachments(request.getAttachments());

        if (request.getFormTemplateId() != null) {
            var template = specialtyFormTemplateRepository.findByIdOptional(request.getFormTemplateId())
                    .orElseThrow(() -> new NotFoundException("Form template no encontrado con id: " + request.getFormTemplateId()));
            record.setFormTemplateId(template.getId());
            record.setFormTemplateVersion(template.getVersion());
        }

        if (request.getAppointmentId() != null) {
            var appointment = medicalAppointmentRepository.findByIdOptional(request.getAppointmentId())
                    .orElseThrow(() -> new NotFoundException("Appointment not found with id: " + request.getAppointmentId()));
            record.setAppointment(appointment);
        }

        if (request.getSpecialtyId() != null) {
            var specialty = specialtyRepository.findByIdOptional(request.getSpecialtyId())
                    .orElseThrow(() -> new NotFoundException("Specialty not found with id: " + request.getSpecialtyId()));
            record.setSpecialty(specialty);
        }

        medicalRecordRepository.persist(record);
        log.info("Medical record created with id: {}", record.getId());

        return toMedicalRecordDto.apply(record);
    }

    @Transactional
    public MedicalRecordDto updateMedicalRecord(UUID recordId, MedicalRecordRequest request) {
        log.info("Updating medical record: {}", recordId);

        var record = medicalRecordRepository.findByIdOptional(recordId)
                .orElseThrow(() -> new NotFoundException("Medical record not found with id: " + recordId));

        if (request.getChiefComplaint() != null) record.setChiefComplaint(request.getChiefComplaint());
        if (request.getPresentIllness() != null) record.setPresentIllness(request.getPresentIllness());
        if (request.getPhysicalExam() != null) record.setPhysicalExam(request.getPhysicalExam());
        if (request.getTreatmentPlan() != null) record.setTreatmentPlan(request.getTreatmentPlan());
        if (request.getVitalSigns() != null) record.setVitalSigns(request.getVitalSigns());
        if (request.getSpecialtyData() != null) record.setSpecialtyData(request.getSpecialtyData());
        if (request.getAttachments() != null) record.setAttachments(request.getAttachments());

        medicalRecordRepository.persist(record);
        log.info("Medical record updated: {}", recordId);

        return toMedicalRecordDto.apply(record);
    }

    @Transactional
    public void deleteMedicalRecord(UUID recordId) {
        log.info("Deleting medical record: {}", recordId);

        var record = medicalRecordRepository.findByIdOptional(recordId)
                .orElseThrow(() -> new NotFoundException("Medical record not found with id: " + recordId));

        medicalRecordRepository.delete(record);
        log.info("Medical record deleted: {}", recordId);
    }

    // Mappers

    public static final Function<MedicalRecordEntity, MedicalRecordDto> toMedicalRecordDto = entity ->
            MedicalRecordDto.builder()
                    .id(entity.getId())
                    .patientId(entity.getPatient().getId())
                    .patientName(entity.getPatient().getFirstName() + " " + entity.getPatient().getLastName())
                    .appointmentId(entity.getAppointment() != null ? entity.getAppointment().getId() : null)
                    .specialtyId(entity.getSpecialty() != null ? entity.getSpecialty().getId() : null)
                    .specialtyName(entity.getSpecialty() != null ? entity.getSpecialty().getName() : null)
                    .doctorId(entity.getDoctor().getId())
                    .doctorName(entity.getDoctor().getFirstName() + " " + entity.getDoctor().getLastName())
                    .recordType(entity.getRecordType() != null ? entity.getRecordType().name() : null)
                    .chiefComplaint(entity.getChiefComplaint())
                    .presentIllness(entity.getPresentIllness())
                    .physicalExam(entity.getPhysicalExam())
                    .treatmentPlan(entity.getTreatmentPlan())
                    .vitalSigns(entity.getVitalSigns())
                    .specialtyData(entity.getSpecialtyData())
                    .formTemplateId(entity.getFormTemplateId())
                    .formTemplateVersion(entity.getFormTemplateVersion())
                    .attachments(entity.getAttachments())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();

}
