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
    PrescriptionRepository prescriptionRepository;

    @Inject
    PrescriptionMedicationRepository prescriptionMedicationRepository;

    @Inject
    MedicationRepository medicationRepository;

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
                .orElse(null);
    }

    public List<MedicalRecordDto> getMedicalRecordsByPatientId(UUID patientId) {
        log.info("Fetching medical records for patient: {}", patientId);

        patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        UUID currentDoctorId = getCurrentDoctorId();
        Stream<MedicalRecordEntity> stream = medicalRecordRepository.findByPatientId(patientId).stream();
        if (currentDoctorId != null) {
            stream = stream.filter(r -> r.getDoctor().getId().equals(currentDoctorId));
        }
        return stream.map(toMedicalRecordDto).collect(Collectors.toList());
    }

    public List<MedicalRecordDto> getMedicalRecordsByAppointmentId(UUID appointmentId) {
        log.info("Fetching medical records for appointment: {}", appointmentId);

        medicalAppointmentRepository.findByIdOptional(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found with id: " + appointmentId));

        UUID currentDoctorId = getCurrentDoctorId();
        Stream<MedicalRecordEntity> stream = medicalRecordRepository.findByAppointmentId(appointmentId).stream();
        if (currentDoctorId != null) {
            stream = stream.filter(r -> r.getDoctor().getId().equals(currentDoctorId));
        }
        return stream.map(toMedicalRecordDto).collect(Collectors.toList());
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
    public MedicalRecordDto createMedicalRecord(UUID patientId, UUID appointmentId, UUID doctorId, UUID specialtyId,
                                                 MedicalRecordType recordType, String chiefComplaint, String presentIllness,
                                                 String physicalExam, String treatmentPlan,
                                                 Map<String, Object> vitalSigns, Map<String, Object> specialtyData,
                                                 Object attachments) {
        log.info("Creating medical record for patient: {}", patientId);

        var patient = patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        var doctor = doctorRepository.findByIdOptional(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + doctorId));

        MedicalRecordEntity record = new MedicalRecordEntity();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setRecordType(recordType != null ? recordType : MedicalRecordType.consultation);
        record.setChiefComplaint(chiefComplaint);
        record.setPresentIllness(presentIllness);
        record.setPhysicalExam(physicalExam);
        record.setTreatmentPlan(treatmentPlan);
        record.setVitalSigns(vitalSigns);
        record.setSpecialtyData(specialtyData);
        record.setAttachments(attachments);

        if (appointmentId != null) {
            var appointment = medicalAppointmentRepository.findByIdOptional(appointmentId)
                    .orElseThrow(() -> new NotFoundException("Appointment not found with id: " + appointmentId));
            record.setAppointment(appointment);
        }

        if (specialtyId != null) {
            var specialty = specialtyRepository.findByIdOptional(specialtyId)
                    .orElseThrow(() -> new NotFoundException("Specialty not found with id: " + specialtyId));
            record.setSpecialty(specialty);
        }

        medicalRecordRepository.persist(record);
        log.info("Medical record created with id: {}", record.getId());

        return toMedicalRecordDto.apply(record);
    }

    @Transactional
    public MedicalRecordDto updateMedicalRecord(UUID recordId, String chiefComplaint, String presentIllness,
                                                 String physicalExam, String treatmentPlan,
                                                 Map<String, Object> vitalSigns, Map<String, Object> specialtyData,
                                                 Object attachments) {
        log.info("Updating medical record: {}", recordId);

        var record = medicalRecordRepository.findByIdOptional(recordId)
                .orElseThrow(() -> new NotFoundException("Medical record not found with id: " + recordId));

        if (chiefComplaint != null) record.setChiefComplaint(chiefComplaint);
        if (presentIllness != null) record.setPresentIllness(presentIllness);
        if (physicalExam != null) record.setPhysicalExam(physicalExam);
        if (treatmentPlan != null) record.setTreatmentPlan(treatmentPlan);
        if (vitalSigns != null) record.setVitalSigns(vitalSigns);
        if (specialtyData != null) record.setSpecialtyData(specialtyData);
        if (attachments != null) record.setAttachments(attachments);

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

    // Prescription methods

    public List<PrescriptionDto> getPrescriptionsByPatientId(UUID patientId) {
        log.info("Fetching prescriptions for patient: {}", patientId);

        patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        UUID currentDoctorId = getCurrentDoctorId();
        Stream<PrescriptionEntity> stream = prescriptionRepository.findByPatientId(patientId).stream();
        if (currentDoctorId != null) {
            stream = stream.filter(p -> p.getDoctor().getId().equals(currentDoctorId));
        }
        return stream.map(toPrescriptionDto).collect(Collectors.toList());
    }

    public List<PrescriptionDto> getActivePrescriptionsByPatientId(UUID patientId) {
        log.info("Fetching active prescriptions for patient: {}", patientId);

        patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        UUID currentDoctorId = getCurrentDoctorId();
        Stream<PrescriptionEntity> stream = prescriptionRepository.findActiveByPatientId(patientId).stream();
        if (currentDoctorId != null) {
            stream = stream.filter(p -> p.getDoctor().getId().equals(currentDoctorId));
        }
        return stream.map(toPrescriptionDto).collect(Collectors.toList());
    }

    public List<PrescriptionDto> getPrescriptionsByMedicalRecordId(UUID medicalRecordId) {
        log.info("Fetching prescriptions for medical record: {}", medicalRecordId);

        return prescriptionRepository.findByMedicalRecordId(medicalRecordId)
                .stream()
                .map(toPrescriptionDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PrescriptionDto createPrescription(UUID patientId, UUID medicalRecordId, UUID doctorId,
                                               List<PrescriptionMedicationRequest> medications, String notes,
                                               LocalDate issueDate, LocalDate expiryDate) {
        log.info("Creating prescription for patient: {}", patientId);

        var patient = patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        var doctor = doctorRepository.findByIdOptional(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + doctorId));

        PrescriptionEntity prescription = new PrescriptionEntity();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setNotes(notes);
        prescription.setIssueDate(issueDate != null ? issueDate : LocalDate.now());
        prescription.setExpiryDate(expiryDate);

        if (medicalRecordId != null) {
            var medicalRecord = medicalRecordRepository.findByIdOptional(medicalRecordId)
                    .orElseThrow(() -> new NotFoundException("Medical record not found with id: " + medicalRecordId));
            prescription.setMedicalRecord(medicalRecord);
        }

        prescriptionRepository.persist(prescription);

        // Add prescription medications
        if (medications != null && !medications.isEmpty()) {
            for (PrescriptionMedicationRequest medRequest : medications) {
                MedicationEntity medication = medicationRepository.findByIdOptional(medRequest.getMedicationId())
                        .orElseThrow(() -> new NotFoundException("Medication not found with id: " + medRequest.getMedicationId()));

                PrescriptionMedicationEntity prescriptionMedication = new PrescriptionMedicationEntity();
                prescriptionMedication.setPrescription(prescription);
                prescriptionMedication.setMedication(medication);
                prescriptionMedication.setDose(medRequest.getDose());
                prescriptionMedication.setFrequency(medRequest.getFrequency());
                prescriptionMedication.setDuration(medRequest.getDuration());
                prescriptionMedication.setQuantity(medRequest.getQuantity());
                prescriptionMedication.setAdministrationRoute(medRequest.getAdministrationRoute());
                prescriptionMedication.setSpecificIndications(medRequest.getSpecificIndications());

                prescriptionMedicationRepository.persist(prescriptionMedication);
                prescription.getPrescriptionMedications().add(prescriptionMedication);
            }
        }

        log.info("Prescription created with id: {}", prescription.getId());

        return toPrescriptionDto.apply(prescription);
    }

    public PrescriptionDto getPrescriptionById(UUID prescriptionId) {
        log.info("Fetching prescription by id: {}", prescriptionId);

        return prescriptionRepository.findByIdOptional(prescriptionId)
                .map(toPrescriptionDto)
                .orElseThrow(() -> new NotFoundException("Prescription not found with id: " + prescriptionId));
    }

    @Transactional
    public void deletePrescription(UUID prescriptionId) {
        log.info("Deleting prescription: {}", prescriptionId);

        var prescription = prescriptionRepository.findByIdOptional(prescriptionId)
                .orElseThrow(() -> new NotFoundException("Prescription not found with id: " + prescriptionId));

        prescriptionRepository.delete(prescription);
        log.info("Prescription deleted: {}", prescriptionId);
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

    public static final Function<PrescriptionMedicationEntity, PrescriptionMedicationDto> toPrescriptionMedicationDto = entity ->
            PrescriptionMedicationDto.builder()
                    .medicationId(entity.getMedication().getId())
                    .medicationName(entity.getMedication().getName())
                    .medicationCode(entity.getMedication().getCode())
                    .concentration(entity.getMedication().getConcentration())
                    .presentation(entity.getMedication().getPresentation())
                    .dose(entity.getDose())
                    .frequency(entity.getFrequency())
                    .duration(entity.getDuration())
                    .quantity(entity.getQuantity())
                    .administrationRoute(entity.getAdministrationRoute())
                    .specificIndications(entity.getSpecificIndications())
                    .build();

    public static final Function<PrescriptionEntity, PrescriptionDto> toPrescriptionDto = entity ->
            PrescriptionDto.builder()
                    .id(entity.getId())
                    .medicalRecordId(entity.getMedicalRecord() != null ? entity.getMedicalRecord().getId() : null)
                    .patientId(entity.getPatient().getId())
                    .patientName(entity.getPatient().getFirstName() + " " + entity.getPatient().getLastName())
                    .doctorId(entity.getDoctor().getId())
                    .doctorName(entity.getDoctor().getFirstName() + " " + entity.getDoctor().getLastName())
                    .medications(entity.getPrescriptionMedications() != null ?
                            entity.getPrescriptionMedications().stream()
                                    .map(toPrescriptionMedicationDto)
                                    .collect(Collectors.toList()) : null)
                    .notes(entity.getNotes())
                    .issueDate(entity.getIssueDate())
                    .expiryDate(entity.getExpiryDate())
                    .createdAt(entity.getCreatedAt())
                    .build();
}
