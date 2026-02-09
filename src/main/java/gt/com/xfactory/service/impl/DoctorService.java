package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.repository.*;
import gt.com.xfactory.utils.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.transaction.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;

import java.time.*;
import java.util.*;
import java.util.function.*;

import static gt.com.xfactory.dto.response.PageResponse.toPageResponse;

@ApplicationScoped
@Slf4j
public class DoctorService {

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    DoctorSpecialtyRepository doctorSpecialtyRepository;

    @Inject
    SpecialtyRepository specialtyRepository;

    @Inject
    ClinicRepository clinicRepository;

    @Inject
    DoctorClinicRepository doctorClinicRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    MedicalAppointmentRepository medicalAppointmentRepository;

    public PageResponse<DoctorDto> getDoctors(DoctorFilterDto filter, @Valid CommonPageRequest pageRequest) {
        log.info("Fetching doctors with filter - pageRequest: {}, filter: {}", pageRequest, filter);

        var fb = FilterBuilder.create()
                .addLike(filter.firstName, "firstName")
                .addLike(filter.lastName, "lastName")
                .addLike(filter.mail, "email", "mail");

        PageResponse<DoctorDto> response = toPageResponse(doctorRepository, fb.buildQuery(), pageRequest, fb.getParams(), toDto);

        // Load specialties and clinics for each doctor
        for (DoctorDto doctor : response.content) {
            List<SpecialtyDto> specialties = doctorSpecialtyRepository.findSpecialtiesByDoctorId(doctor.getId());
            doctor.setSpecialties(specialties);

            doctor.setClinics(getActiveClinics(doctorClinicRepository.findByDoctorId(doctor.getId())));
        }

        return response;
    }

    public DoctorDto getDoctorById(UUID id) {
        log.info("Fetching doctor by id: {}", id);
        DoctorEntity doctor = doctorRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + id));

        DoctorDto dto = toDto.apply(doctor);
        dto.setSpecialties(doctorSpecialtyRepository.findSpecialtiesByDoctorId(id));
        dto.setClinics(getActiveClinics(doctorClinicRepository.findByDoctorId(id)));
        return dto;
    }

    @Transactional
    public DoctorDto createDoctor(DoctorRequest request) {
        log.info("Creating doctor: {} {}", request.getFirstName(), request.getLastName());

        DoctorEntity doctor = new DoctorEntity();
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setBirthdate(request.getBirthdate());
        doctor.setAddress(request.getAddress());
        doctor.setEmail(request.getMail());
        doctor.setPhone(request.getPhone());
        doctor.setCreatedAt(LocalDate.now());

        if (request.getMail() != null) {
            userRepository.findByEmail(request.getMail()).ifPresent(doctor::setUser);
        }

        doctorRepository.persist(doctor);
        log.info("Doctor created with id: {}", doctor.getId());

        return toDto.apply(doctor);
    }

    @Transactional
    public DoctorDto updateDoctor(UUID id, DoctorRequest request) {
        log.info("Updating doctor with id: {}", id);

        DoctorEntity doctor = doctorRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + id));

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setBirthdate(request.getBirthdate());
        doctor.setAddress(request.getAddress());
        doctor.setEmail(request.getMail());
        doctor.setPhone(request.getPhone());
        doctor.setUpdatedAt(LocalDate.now());

        // Sincronizar email en usuario vinculado
        if (doctor.getUser() != null && request.getMail() != null
                && !request.getMail().equals(doctor.getUser().getEmail())) {
            doctor.getUser().setEmail(request.getMail());
        }

        if (request.getMail() != null) {
            userRepository.findByEmail(request.getMail()).ifPresentOrElse(
                    doctor::setUser,
                    () -> doctor.setUser(null)
            );
        } else {
            doctor.setUser(null);
        }

        doctorRepository.persist(doctor);

        DoctorDto dto = toDto.apply(doctor);
        dto.setSpecialties(doctorSpecialtyRepository.findSpecialtiesByDoctorId(id));
        dto.setClinics(getActiveClinics(doctorClinicRepository.findByDoctorId(id)));
        return dto;
    }

    @Transactional
    public void deleteDoctor(UUID id) {
        log.info("Deleting doctor with id: {}", id);

        DoctorEntity doctor = doctorRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + id));

        List<String> relatedData = new ArrayList<>();

        if (medicalAppointmentRepository.count("doctor.id", id) > 0) {
            relatedData.add("citas médicas");
        }

        if (!relatedData.isEmpty()) {
            throw new IllegalStateException(
                    "No se puede eliminar el doctor porque tiene datos relacionados: " + String.join(", ", relatedData));
        }

        // Delete doctor specialties first
        doctorSpecialtyRepository.deleteByDoctorId(id);

        doctorRepository.delete(doctor);
        log.info("Doctor deleted successfully");
    }

    // ============ Specialty Management ============

    public List<SpecialtyDto> getDoctorSpecialties(UUID doctorId) {
        log.info("Fetching specialties for doctor: {}", doctorId);

        // Verify doctor exists
        doctorRepository.findByIdOptional(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + doctorId));

        return doctorSpecialtyRepository.findSpecialtiesByDoctorId(doctorId);
    }

    @Transactional
    public SpecialtyDto addSpecialtyToDoctor(UUID doctorId, UUID specialtyId) {
        log.info("Adding specialty {} to doctor {}", specialtyId, doctorId);

        DoctorEntity doctor = doctorRepository.findByIdOptional(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + doctorId));

        SpecialtyEntity specialty = specialtyRepository.findByIdOptional(specialtyId)
                .orElseThrow(() -> new NotFoundException("Specialty not found with id: " + specialtyId));

        // Check if already assigned
        var existing = doctorSpecialtyRepository.findByDoctorIdAndSpecialtyId(doctorId, specialtyId);
        if (existing.isPresent()) {
            log.info("Specialty already assigned to doctor");
            return SpecialtyDto.builder()
                    .id(specialty.getId())
                    .name(specialty.getName())
                    .description(specialty.getDescription())
                    .build();
        }

        DoctorSpecialtyEntity doctorSpecialty = new DoctorSpecialtyEntity();
        doctorSpecialty.setId(new DoctorSpecialtyId(doctorId, specialtyId));
        doctorSpecialty.setDoctor(doctor);
        doctorSpecialty.setSpecialty(specialty);

        doctorSpecialtyRepository.persist(doctorSpecialty);
        log.info("Specialty added to doctor successfully");

        return SpecialtyDto.builder()
                .id(specialty.getId())
                .name(specialty.getName())
                .description(specialty.getDescription())
                .build();
    }

    @Transactional
    public void removeSpecialtyFromDoctor(UUID doctorId, UUID specialtyId) {
        log.info("Removing specialty {} from doctor {}", specialtyId, doctorId);

        DoctorSpecialtyEntity doctorSpecialty = doctorSpecialtyRepository
                .findByDoctorIdAndSpecialtyId(doctorId, specialtyId)
                .orElseThrow(() -> new NotFoundException(
                        "Specialty " + specialtyId + " not assigned to doctor " + doctorId));

        doctorSpecialtyRepository.delete(doctorSpecialty);
        log.info("Specialty removed from doctor successfully");
    }

    // ============ Clinic Management ============

    public List<ClinicDto> getDoctorClinics(UUID doctorId) {
        log.info("Fetching clinics for doctor: {}", doctorId);

        doctorRepository.findByIdOptional(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + doctorId));

        return getActiveClinics(doctorClinicRepository.findByDoctorId(doctorId));
    }

    @Transactional
    public ClinicDto addClinicToDoctor(UUID doctorId, UUID clinicId) {
        log.info("Adding clinic {} to doctor {}", clinicId, doctorId);

        DoctorEntity doctor = doctorRepository.findByIdOptional(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + doctorId));

        ClinicEntity clinic = clinicRepository.findByIdOptional(clinicId)
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + clinicId));

        var existing = doctorClinicRepository.findByDoctorIdAndClinicId(doctorId, clinicId);
        if (existing.isPresent()) {
            DoctorClinicEntity dc = existing.get();
            if (dc.getActive() != null && dc.getActive()) {
                log.info("Clinic already assigned to doctor");
            } else {
                dc.setActive(true);
                dc.setAssignedAt(java.time.LocalDateTime.now());
                dc.setUnassignedAt(null);
                doctorClinicRepository.persist(dc);
                log.info("Clinic reactivated for doctor");
            }
            return ClinicDto.builder()
                    .id(clinic.getId())
                    .name(clinic.getName())
                    .address(clinic.getAddress())
                    .phone(clinic.getPhone())
                    .build();
        }

        DoctorClinicEntity doctorClinic = new DoctorClinicEntity();
        doctorClinic.setId(new DoctorClinicId(doctorId, clinicId));
        doctorClinic.setDoctor(doctor);
        doctorClinic.setClinic(clinic);
        doctorClinic.setAssignedAt(java.time.LocalDateTime.now());
        doctorClinic.setActive(true);

        doctorClinicRepository.persist(doctorClinic);
        log.info("Clinic added to doctor successfully");

        return ClinicDto.builder()
                .id(clinic.getId())
                .name(clinic.getName())
                .address(clinic.getAddress())
                .phone(clinic.getPhone())
                .build();
    }

    @Transactional
    public void removeClinicFromDoctor(UUID doctorId, UUID clinicId) {
        log.info("Removing clinic {} from doctor {}", clinicId, doctorId);

        DoctorClinicEntity doctorClinic = doctorClinicRepository
                .findByDoctorIdAndClinicId(doctorId, clinicId)
                .orElseThrow(() -> new NotFoundException(
                        "Clinic " + clinicId + " not assigned to doctor " + doctorId));

        doctorClinic.setActive(false);
        doctorClinic.setUnassignedAt(java.time.LocalDateTime.now());
        doctorClinicRepository.persist(doctorClinic);
        log.info("Clinic removed from doctor successfully");
    }

    public DoctorDto getDoctorByUserId(UUID userId) {
        log.info("Fetching doctor by userId: {}", userId);
        DoctorEntity doctor = doctorRepository.find("user.id", userId).firstResultOptional()
                .orElseThrow(() -> new NotFoundException("Doctor not found for userId: " + userId));

        DoctorDto dto = toDto.apply(doctor);
        dto.setSpecialties(doctorSpecialtyRepository.findSpecialtiesByDoctorId(doctor.getId()));
        dto.setClinics(getActiveClinics(doctorClinicRepository.findByDoctorId(doctor.getId())));
        return dto;
    }

    public static ClinicDto toClinicDto(DoctorClinicEntity dc) {
        return ClinicDto.builder()
                .id(dc.getClinic().getId())
                .name(dc.getClinic().getName())
                .address(dc.getClinic().getAddress())
                .phone(dc.getClinic().getPhone())
                .build();
    }

    public static List<ClinicDto> getActiveClinics(List<DoctorClinicEntity> doctorClinics) {
        return doctorClinics.stream()
                .filter(dc -> dc.getActive() != null && dc.getActive())
                .map(DoctorService::toClinicDto)
                .toList();
    }

    public static final Function<DoctorEntity, DoctorDto> toDto = entity ->
    {
        int age = 0;
        if (entity.getBirthdate() != null) {
            age = Period.between(entity.getBirthdate(), LocalDate.now()).getYears();
        }
        return DoctorDto.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .birthdate(entity.getBirthdate())
                .age(age)
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .mail(entity.getEmail())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .build();
    };
}
