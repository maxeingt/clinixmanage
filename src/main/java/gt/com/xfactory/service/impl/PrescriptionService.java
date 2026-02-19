package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.repository.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.transaction.*;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@ApplicationScoped
@Slf4j
public class PrescriptionService {

    @Inject
    PrescriptionRepository prescriptionRepository;

    @Inject
    PrescriptionMedicationRepository prescriptionMedicationRepository;

    @Inject
    MedicationRepository medicationRepository;

    @Inject
    PatientRepository patientRepository;

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    MedicalRecordRepository medicalRecordRepository;

    @Inject
    SecurityContextService securityContextService;

    private UUID getCurrentDoctorId() {
        return securityContextService.getCurrentDoctorId();
    }

    public List<PrescriptionDto> getPrescriptionsByPatientId(UUID patientId) {
        log.info("Fetching prescriptions for patient: {}", patientId);

        patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        UUID currentDoctorId = getCurrentDoctorId();
        List<PrescriptionEntity> prescriptions = currentDoctorId != null
                ? prescriptionRepository.findByPatientIdAndDoctorId(patientId, currentDoctorId)
                : prescriptionRepository.findByPatientId(patientId);
        return prescriptions.stream().map(toPrescriptionDto).collect(Collectors.toList());
    }

    public List<PrescriptionDto> getActivePrescriptionsByPatientId(UUID patientId) {
        log.info("Fetching active prescriptions for patient: {}", patientId);

        patientRepository.findByIdOptional(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + patientId));

        UUID currentDoctorId = getCurrentDoctorId();
        List<PrescriptionEntity> prescriptions = currentDoctorId != null
                ? prescriptionRepository.findActiveByPatientIdAndDoctorId(patientId, currentDoctorId)
                : prescriptionRepository.findActiveByPatientId(patientId);
        return prescriptions.stream().map(toPrescriptionDto).collect(Collectors.toList());
    }

    public List<PrescriptionDto> getPrescriptionsByMedicalRecordId(UUID medicalRecordId) {
        log.info("Fetching prescriptions for medical record: {}", medicalRecordId);

        UUID currentDoctorId = getCurrentDoctorId();
        List<PrescriptionEntity> prescriptions = prescriptionRepository.findByMedicalRecordId(medicalRecordId);

        if (currentDoctorId != null) {
            prescriptions = prescriptions.stream()
                    .filter(p -> currentDoctorId.equals(p.getDoctor().getId()))
                    .toList();
        }

        return prescriptions.stream().map(toPrescriptionDto).collect(Collectors.toList());
    }

    @Transactional
    public PrescriptionDto createPrescription(PrescriptionRequest request) {
        log.info("Creating prescription for patient: {}", request.getPatientId());

        var patient = patientRepository.findByIdOptional(request.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + request.getPatientId()));

        var doctor = doctorRepository.findByIdOptional(request.getDoctorId())
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + request.getDoctorId()));

        PrescriptionEntity prescription = new PrescriptionEntity();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setNotes(request.getNotes());
        prescription.setIssueDate(request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now());
        prescription.setExpiryDate(request.getExpiryDate());

        if (request.getMedicalRecordId() != null) {
            var medicalRecord = medicalRecordRepository.findByIdOptional(request.getMedicalRecordId())
                    .orElseThrow(() -> new NotFoundException("Medical record not found with id: " + request.getMedicalRecordId()));
            prescription.setMedicalRecord(medicalRecord);
        }

        prescriptionRepository.persist(prescription);

        // Add prescription medications
        if (request.getMedications() != null && !request.getMedications().isEmpty()) {
            for (PrescriptionMedicationRequest medRequest : request.getMedications()) {
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

        PrescriptionEntity prescription = prescriptionRepository.findByIdOptional(prescriptionId)
                .orElseThrow(() -> new NotFoundException("Prescription not found with id: " + prescriptionId));

        securityContextService.validateDoctorOwnership(prescription.getDoctor().getId());

        return toPrescriptionDto.apply(prescription);
    }

    @Transactional
    public void deletePrescription(UUID prescriptionId) {
        log.info("Deleting prescription: {}", prescriptionId);

        var prescription = prescriptionRepository.findByIdOptional(prescriptionId)
                .orElseThrow(() -> new NotFoundException("Prescription not found with id: " + prescriptionId));

        securityContextService.validateDoctorOwnership(prescription.getDoctor().getId());

        prescriptionRepository.delete(prescription);
        log.info("Prescription deleted: {}", prescriptionId);
    }

    // Mappers

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
