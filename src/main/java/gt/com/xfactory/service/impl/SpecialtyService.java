package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.repository.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

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

        specialtyRepository.findByIdOptional(specialtyId)
                .orElseThrow(() -> new NotFoundException("Specialty not found with id: " + specialtyId));

        List<DoctorEntity> doctors = doctorSpecialtyRepository.findDoctorsBySpecialtyId(specialtyId);

        if (doctors.isEmpty()) return Collections.emptyList();

        // Batch load specialties (1 query instead of N)
        List<UUID> doctorIds = doctors.stream().map(DoctorEntity::getId).toList();
        Map<UUID, List<SpecialtyDto>> specialtiesMap = DoctorService.toSpecialtyDtoMap(doctorSpecialtyRepository.findByDoctorIds(doctorIds));

        return doctors.stream()
                .map(entity -> {
                    DoctorDto dto = DoctorService.toDto.apply(entity);
                    dto.setSpecialties(specialtiesMap.getOrDefault(entity.getId(), Collections.emptyList()));
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
}
