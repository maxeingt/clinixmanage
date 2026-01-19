package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.dto.request.filter.DoctorFilterDto;
import gt.com.xfactory.dto.response.DoctorDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.dto.response.SpecialtyDto;
import gt.com.xfactory.repository.DoctorClinicRepository;
import gt.com.xfactory.repository.DoctorSpecialtyRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class ClinicService {

    @Inject
    DoctorClinicRepository doctorClinicRepository;

    @Inject
    DoctorSpecialtyRepository doctorSpecialtyRepository;

    public PageResponse<DoctorDto> getDoctorsByClinic(UUID clinic, DoctorFilterDto filter, @Valid CommonPageRequest pageRequest) {
        PageResponse<DoctorDto> response = doctorClinicRepository.findDoctorsByClinic(clinic, filter, pageRequest);

        // Load specialties for each doctor
        for (DoctorDto doctor : response.content) {
            List<SpecialtyDto> specialties = doctorSpecialtyRepository.findSpecialtiesByDoctorId(doctor.getId());
            doctor.setSpecialties(specialties);
        }

        return response;
    }

}
