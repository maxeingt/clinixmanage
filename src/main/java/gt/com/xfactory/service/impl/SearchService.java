package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.response.GlobalSearchDto;
import gt.com.xfactory.dto.response.GlobalSearchDto.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.repository.*;
import gt.com.xfactory.utils.FilterBuilder;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

@ApplicationScoped
@Slf4j
public class SearchService {

    private static final int MAX_RESULTS_PER_TYPE = 5;

    @Inject
    PatientRepository patientRepository;

    @Inject
    MedicalAppointmentRepository medicalAppointmentRepository;

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    ClinicRepository clinicRepository;

    @Inject
    MedicalRecordRepository medicalRecordRepository;

    @Inject
    MedicationRepository medicationRepository;

    @Inject
    DoctorSpecialtyRepository doctorSpecialtyRepository;

    @Inject
    SecurityContextService securityContextService;

    public GlobalSearchDto search(String q, List<String> types) {
        if (q == null || q.trim().length() < 3) {
            throw new BadRequestException("El término de búsqueda debe tener al menos 3 caracteres");
        }

        String term = q.trim();
        Set<String> requestedTypes = (types == null || types.isEmpty())
                ? Set.of("patients", "appointments", "doctors", "clinics", "records", "medications")
                : new HashSet<>(types);

        UUID currentDoctorId = securityContextService.getCurrentDoctorId();

        GlobalSearchDto.GlobalSearchDtoBuilder builder = GlobalSearchDto.builder();

        if (requestedTypes.contains("patients")) {
            builder.patients(searchPatients(term));
        }
        if (requestedTypes.contains("appointments")) {
            builder.appointments(searchAppointments(term, currentDoctorId));
        }
        if (requestedTypes.contains("doctors")) {
            builder.doctors(searchDoctors(term));
        }
        if (requestedTypes.contains("clinics")) {
            builder.clinics(searchClinics(term));
        }
        if (requestedTypes.contains("records")) {
            builder.records(searchRecords(term, currentDoctorId));
        }
        if (requestedTypes.contains("medications")) {
            builder.medications(searchMedications(term));
        }

        return builder.build();
    }

    private List<PatientResult> searchPatients(String q) {
        Map<String, Object> params = new HashMap<>();
        String nameCondition = FilterBuilder.buildNameTokenCondition(q, "firstName", "lastName", params, "n");
        params.put("dpi", q);
        params.put("phone", q);

        return patientRepository.find(
                nameCondition +
                " OR dpi LIKE CONCAT('%', :dpi, '%')" +
                " OR phone LIKE CONCAT('%', :phone, '%')",
                params)
                .page(0, MAX_RESULTS_PER_TYPE)
                .stream()
                .map(p -> PatientResult.builder()
                        .id(p.getId())
                        .name(p.getFirstName() + " " + p.getLastName())
                        .dpi(p.getDpi())
                        .age(p.getBirthdate() != null
                                ? Period.between(p.getBirthdate(), LocalDate.now()).getYears()
                                : 0)
                        .build())
                .collect(Collectors.toList());
    }

    private List<AppointmentResult> searchAppointments(String q, UUID currentDoctorId) {
        Map<String, Object> params = new HashMap<>();
        String patientNameCond = FilterBuilder.buildNameTokenCondition(q, "patient.firstName", "patient.lastName", params, "pat");
        String doctorNameCond = FilterBuilder.buildNameTokenCondition(q, "doctor.firstName", "doctor.lastName", params, "doc");

        StringBuilder jpql = new StringBuilder("(" + patientNameCond + " OR " + doctorNameCond + ")");

        if (currentDoctorId != null) {
            jpql.append(" AND doctor.id = :doctorId");
            params.put("doctorId", currentDoctorId);
        }

        jpql.append(" ORDER BY appointmentDate DESC");

        return medicalAppointmentRepository.find(jpql.toString(), params)
                .page(0, MAX_RESULTS_PER_TYPE)
                .stream()
                .map(a -> AppointmentResult.builder()
                        .id(a.getId())
                        .patientId(a.getPatient().getId())
                        .patientName(a.getPatient().getFirstName() + " " + a.getPatient().getLastName())
                        .doctorName(a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName())
                        .date(a.getAppointmentDate())
                        .status(a.getStatus() != null ? a.getStatus().name() : null)
                        .build())
                .collect(Collectors.toList());
    }

    private List<DoctorResult> searchDoctors(String q) {
        Map<String, Object> params = new HashMap<>();
        String condition = FilterBuilder.buildNameTokenCondition(q, "firstName", "lastName", params, "d");

        List<DoctorEntity> doctors = doctorRepository.find(condition, params)
                .page(0, MAX_RESULTS_PER_TYPE)
                .list();

        List<UUID> doctorIds = doctors.stream().map(DoctorEntity::getId).collect(Collectors.toList());
        Map<UUID, List<DoctorSpecialtyEntity>> specialtyMap = doctorSpecialtyRepository.findByDoctorIds(doctorIds);

        return doctors.stream()
                .map(d -> {
                    List<DoctorSpecialtyEntity> specs = specialtyMap.getOrDefault(d.getId(), Collections.emptyList());
                    String specialty = specs.isEmpty() ? null : specs.get(0).getSpecialty().getName();
                    return DoctorResult.builder()
                            .id(d.getId())
                            .name(d.getFirstName() + " " + d.getLastName())
                            .specialty(specialty)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<ClinicResult> searchClinics(String q) {
        return clinicRepository.find(
                "LOWER(name) LIKE LOWER(CONCAT('%', :q, '%')) " +
                "OR LOWER(address) LIKE LOWER(CONCAT('%', :q, '%'))",
                Map.of("q", q))
                .page(0, MAX_RESULTS_PER_TYPE)
                .stream()
                .map(c -> ClinicResult.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .address(c.getAddress())
                        .build())
                .collect(Collectors.toList());
    }

    private List<RecordResult> searchRecords(String q, UUID currentDoctorId) {
        Map<String, Object> params = new HashMap<>();
        String patientNameCond = FilterBuilder.buildNameTokenCondition(q, "patient.firstName", "patient.lastName", params, "pat");
        params.put("complaint", q);

        StringBuilder jpql = new StringBuilder(
                "(" + patientNameCond + " OR LOWER(chiefComplaint) LIKE LOWER(CONCAT('%', :complaint, '%')))"
        );

        if (currentDoctorId != null) {
            jpql.append(" AND doctor.id = :doctorId");
            params.put("doctorId", currentDoctorId);
        }

        jpql.append(" ORDER BY createdAt DESC");

        return medicalRecordRepository.find(jpql.toString(), params)
                .page(0, MAX_RESULTS_PER_TYPE)
                .stream()
                .map(r -> RecordResult.builder()
                        .id(r.getId())
                        .patientId(r.getPatient().getId())
                        .appointmentId(r.getAppointment() != null ? r.getAppointment().getId() : null)
                        .patientName(r.getPatient().getFirstName() + " " + r.getPatient().getLastName())
                        .appointmentDate(r.getAppointment() != null ? r.getAppointment().getAppointmentDate() : r.getCreatedAt())
                        .diagnosis(r.getChiefComplaint())
                        .build())
                .collect(Collectors.toList());
    }

    private List<MedicationResult> searchMedications(String q) {
        return medicationRepository.find(
                "active = true AND (" +
                "LOWER(name) LIKE LOWER(CONCAT('%', :q, '%')) " +
                "OR LOWER(code) LIKE LOWER(CONCAT('%', :q, '%')) " +
                "OR LOWER(concentration) LIKE LOWER(CONCAT('%', :q, '%')) " +
                "OR LOWER(activeIngredient) LIKE LOWER(CONCAT('%', :q, '%')))",
                Map.of("q", q))
                .page(0, MAX_RESULTS_PER_TYPE)
                .stream()
                .map(m -> MedicationResult.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .code(m.getCode())
                        .concentration(m.getConcentration())
                        .build())
                .collect(Collectors.toList());
    }
}
