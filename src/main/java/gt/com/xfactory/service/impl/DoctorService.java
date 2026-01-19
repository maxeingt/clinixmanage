package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.dto.request.filter.DoctorFilterDto;
import gt.com.xfactory.dto.response.DoctorDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.dto.response.SpecialtyDto;
import gt.com.xfactory.entity.DoctorEntity;
import gt.com.xfactory.repository.DoctorRepository;
import gt.com.xfactory.repository.DoctorSpecialtyRepository;
import gt.com.xfactory.utils.QueryUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static gt.com.xfactory.dto.response.PageResponse.toPageResponse;

@ApplicationScoped
@Slf4j
public class DoctorService {

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    DoctorSpecialtyRepository doctorSpecialtyRepository;

    public PageResponse<DoctorDto> getDoctors(DoctorFilterDto filter, @Valid CommonPageRequest pageRequest) {
        log.info("Fetching doctors with filter - pageRequest: {}, filter: {}", pageRequest, filter);

        StringBuilder query = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();

        QueryUtils.addLikeCondition(filter.firstName, "firstName", "firstName", conditions, params);
        QueryUtils.addLikeCondition(filter.lastName, "lastName", "lastName", conditions, params);
        QueryUtils.addLikeCondition(filter.mail, "email", "mail", conditions, params);

        if (!conditions.isEmpty()) {
            query.append(String.join(" AND ", conditions));
        }

        PageResponse<DoctorDto> response = toPageResponse(doctorRepository, query, pageRequest, params, toDto);

        // Load specialties for each doctor
        for (DoctorDto doctor : response.content) {
            List<SpecialtyDto> specialties = doctorSpecialtyRepository.findSpecialtiesByDoctorId(doctor.getId());
            doctor.setSpecialties(specialties);
        }

        return response;
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
                .build();
    };
}
