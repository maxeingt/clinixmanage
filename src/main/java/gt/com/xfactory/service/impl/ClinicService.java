package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.ClinicRequest;
import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.dto.request.filter.DoctorFilterDto;
import gt.com.xfactory.dto.request.filter.MedicalAppointmentFilterDto;
import gt.com.xfactory.dto.response.ClinicDto;
import gt.com.xfactory.dto.response.DoctorDto;
import gt.com.xfactory.dto.response.MedicalAppointmentDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.dto.response.SpecialtyDto;
import gt.com.xfactory.entity.ClinicEntity;
import gt.com.xfactory.repository.ClinicRepository;
import gt.com.xfactory.repository.DoctorClinicRepository;
import gt.com.xfactory.repository.DoctorSpecialtyRepository;
import gt.com.xfactory.repository.MedicalAppointmentRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static gt.com.xfactory.service.impl.PatientService.toMedicalAppointmentDto;

@ApplicationScoped
@Slf4j
public class ClinicService {

    @Inject
    ClinicRepository clinicRepository;

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
        PageResponse<DoctorDto> response = doctorClinicRepository.findDoctorsByClinic(clinic, filter, pageRequest);

        // Load specialties for each doctor
        for (DoctorDto doctor : response.content) {
            List<SpecialtyDto> specialties = doctorSpecialtyRepository.findSpecialtiesByDoctorId(doctor.getId());
            doctor.setSpecialties(specialties);
        }

        return response;
    }

    public List<MedicalAppointmentDto> getAppointmentsByClinic(UUID clinicId, MedicalAppointmentFilterDto filter) {
        log.info("Fetching appointments for clinic: {} with filter - startDate: {}, endDate: {}",
                clinicId, filter != null ? filter.startDate : null, filter != null ? filter.endDate : null);

        return medicalAppointmentRepository.findByClinicId(clinicId, filter)
                .stream()
                .map(toMedicalAppointmentDto)
                .collect(Collectors.toList());
    }
}
