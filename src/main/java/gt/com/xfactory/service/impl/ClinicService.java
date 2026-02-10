package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.repository.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.transaction.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;

import java.util.*;
import java.util.stream.*;

@ApplicationScoped
@Slf4j
public class ClinicService {

    @Inject
    ClinicRepository clinicRepository;

    @Inject
    MedicalAppointmentService medicalAppointmentService;

    @Inject
    DoctorClinicRepository doctorClinicRepository;

    @Inject
    DoctorSpecialtyRepository doctorSpecialtyRepository;

    @Inject
    MedicalAppointmentRepository medicalAppointmentRepository;

    public List<ClinicDto> getAllClinics() {
        log.info("Fetching all clinics");
        return clinicRepository.listAll()
                .stream()
                .map(this::toClinicDto)
                .collect(Collectors.toList());
    }

    public ClinicDto getClinicById(UUID id) {
        log.info("Fetching clinic by id: {}", id);
        return clinicRepository.findByIdOptional(id)
                .map(this::toClinicDto)
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + id));
    }

    @Transactional
    public ClinicDto createClinic(ClinicRequest request) {
        log.info("Creating clinic with name: {}", request.getName());

        ClinicEntity clinic = new ClinicEntity();
        clinic.setName(request.getName());
        clinic.setAddress(request.getAddress());
        clinic.setPhone(request.getPhone());

        clinicRepository.persist(clinic);
        log.info("Clinic created with id: {}", clinic.getId());

        return toClinicDto(clinic);
    }

    @Transactional
    public ClinicDto updateClinic(UUID id, ClinicRequest request) {
        log.info("Updating clinic with id: {}", id);

        ClinicEntity clinic = clinicRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + id));

        clinic.setName(request.getName());
        clinic.setAddress(request.getAddress());
        clinic.setPhone(request.getPhone());

        clinicRepository.persist(clinic);
        return toClinicDto(clinic);
    }

    @Transactional
    public void deleteClinic(UUID id) {
        log.info("Deleting clinic with id: {}", id);

        ClinicEntity clinic = clinicRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + id));

        clinicRepository.delete(clinic);
    }

    private ClinicDto toClinicDto(ClinicEntity entity) {
        ClinicDto dto = new ClinicDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setAddress(entity.getAddress());
        dto.setPhone(entity.getPhone());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public PageResponse<DoctorDto> getDoctorsByClinic(UUID clinic, DoctorFilterDto filter, @Valid CommonPageRequest pageRequest) {
        PageResponse<DoctorEntity> entityResponse = doctorClinicRepository.findDoctorsByClinic(clinic, filter, pageRequest);

        List<DoctorDto> dtos = entityResponse.content.stream()
                .map(DoctorService.toDto)
                .toList();

        // Batch load specialties (1 query instead of N)
        if (!dtos.isEmpty()) {
            List<UUID> doctorIds = dtos.stream().map(DoctorDto::getId).toList();
            Map<UUID, List<SpecialtyDto>> specialtiesMap = DoctorService.toSpecialtyDtoMap(doctorSpecialtyRepository.findByDoctorIds(doctorIds));
            for (DoctorDto doctor : dtos) {
                doctor.setSpecialties(specialtiesMap.getOrDefault(doctor.getId(), Collections.emptyList()));
            }
        }

        return new PageResponse<>(dtos, entityResponse.currentPage, entityResponse.totalPages, entityResponse.totalItems);
    }

    public List<MedicalAppointmentDto> getAppointmentsByClinic(UUID clinicId, MedicalAppointmentFilterDto filter) {
        log.info("Fetching appointments for clinic: {} with filter - startDate: {}, endDate: {}",
                clinicId, filter != null ? filter.startDate : null, filter != null ? filter.endDate : null);

        return medicalAppointmentRepository.findByClinicId(clinicId, filter)
                .stream()
                .map(medicalAppointmentService::toMedicalAppointmentDto)
                .collect(Collectors.toList());
    }
}
