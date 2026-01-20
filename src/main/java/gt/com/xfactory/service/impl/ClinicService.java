package gt.com.xfactory.service.impl;

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
import jakarta.validation.Valid;
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
