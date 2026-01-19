package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.response.DoctorDto;
import gt.com.xfactory.dto.response.SpecialtyDto;
import gt.com.xfactory.entity.DoctorEntity;
import gt.com.xfactory.entity.SpecialtyEntity;
import gt.com.xfactory.repository.DoctorSpecialtyRepository;
import gt.com.xfactory.repository.SpecialtyRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class SpecialtyService {

    @Inject
    SpecialtyRepository specialtyRepository;

    @Inject
    DoctorSpecialtyRepository doctorSpecialtyRepository;

    public List<SpecialtyDto> getAllSpecialties() {
        log.info("Fetching all specialties");
        return specialtyRepository.listAll()
                .stream()
                .map(toSpecialtyDto)
                .collect(Collectors.toList());
    }

    public SpecialtyDto getSpecialtyById(UUID specialtyId) {
        log.info("Fetching specialty by id: {}", specialtyId);
        return specialtyRepository.findByIdOptional(specialtyId)
                .map(toSpecialtyDto)
                .orElseThrow(() -> new NotFoundException("Specialty not found with id: " + specialtyId));
    }

    public List<DoctorDto> getDoctorsBySpecialtyId(UUID specialtyId) {
        log.info("Fetching doctors by specialty id: {}", specialtyId);

        // Verify specialty exists
        specialtyRepository.findByIdOptional(specialtyId)
                .orElseThrow(() -> new NotFoundException("Specialty not found with id: " + specialtyId));

        List<DoctorEntity> doctors = doctorSpecialtyRepository.findDoctorsBySpecialtyId(specialtyId);

        return doctors.stream()
                .map(entity -> {
                    DoctorDto dto = toDoctorDto.apply(entity);
                    // Load specialties for each doctor
                    dto.setSpecialties(doctorSpecialtyRepository.findSpecialtiesByDoctorId(entity.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public static final Function<SpecialtyEntity, SpecialtyDto> toSpecialtyDto = entity ->
            SpecialtyDto.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .build();

    public static final Function<DoctorEntity, DoctorDto> toDoctorDto = entity -> {
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
