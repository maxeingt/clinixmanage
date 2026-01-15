package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.dto.request.filter.DoctorFilterDto;
import gt.com.xfactory.dto.response.DoctorDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.repository.DoctorClinicRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@ApplicationScoped
@Slf4j
public class ClinicService {

    @Inject
    DoctorClinicRepository doctorClinicRepository;

    public PageResponse<DoctorDto> getDoctorsByClinic(UUID clinic, DoctorFilterDto filter, @Valid CommonPageRequest pageRequest) {
       return doctorClinicRepository.findDoctorsByClinic(clinic, filter, pageRequest);
    }

}
