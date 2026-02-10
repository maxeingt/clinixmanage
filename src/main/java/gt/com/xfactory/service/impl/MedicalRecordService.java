package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.entity.enums.*;
import gt.com.xfactory.repository.*;
import io.quarkus.security.identity.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.transaction.*;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;
import org.eclipse.microprofile.jwt.*;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

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

    // Specialty Form Template methods

    public List<SpecialtyFormTemplateDto> getFormTemplatesBySpecialtyId(UUID specialtyId) {
        log.info("Fetching form templates for specialty: {}", specialtyId);

        return specialtyFormTemplateRepository.findActiveBySpecialtyId(specialtyId)
                .stream()
                .map(toSpecialtyFormTemplateDto)
                .collect(Collectors.toList());
    }

    public List<SpecialtyFormTemplateDto> getAllActiveFormTemplates() {
        log.info("Fetching all active form templates");

        return specialtyFormTemplateRepository.findAllActive()
                .stream()
                .map(toSpecialtyFormTemplateDto)
                .collect(Collectors.toList());
    }

    public SpecialtyFormTemplateDto getFormTemplateById(UUID templateId) {
        log.info("Fetching form template by id: {}", templateId);

        return specialtyFormTemplateRepository.findByIdOptional(templateId)
                .map(toSpecialtyFormTemplateDto)
                .orElseThrow(() -> new NotFoundException("Form template not found with id: " + templateId));
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
                    .attachments(entity.getAttachments())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();

    public static final Function<SpecialtyFormTemplateEntity, SpecialtyFormTemplateDto> toSpecialtyFormTemplateDto = entity ->
            SpecialtyFormTemplateDto.builder()
                    .id(entity.getId())
                    .specialtyId(entity.getSpecialty().getId())
                    .specialtyName(entity.getSpecialty().getName())
                    .formName(entity.getFormName())
                    .description(entity.getDescription())
                    .formSchema(entity.getFormSchema())
                    .isActive(entity.getIsActive())
                    .version(entity.getVersion())
                    .build();

}
