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

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static gt.com.xfactory.dto.response.PageResponse.toPageResponse;

@ApplicationScoped
@Slf4j
public class PharmaceuticalService {

    @Inject
    PharmaceuticalRepository pharmaceuticalRepository;

    public PageResponse<PharmaceuticalDto> getPharmaceuticals(PharmaceuticalFilterDto filter, @Valid CommonPageRequest pageRequest) {
        log.info("Fetching pharmaceuticals with filter - pageRequest: {}, filter: {}", pageRequest, filter);

        var fb = FilterBuilder.create()
                .addLike(filter.name, "name")
                .addEquals(filter.active, "active");

        return toPageResponse(pharmaceuticalRepository, fb.buildQuery(), pageRequest, fb.getParams(), toDto);
    }

    public PharmaceuticalDto getById(UUID id) {
        log.info("Fetching pharmaceutical by id: {}", id);
        return pharmaceuticalRepository.findByIdOptional(id)
                .map(toDto)
                .orElseThrow(() -> new NotFoundException("Pharmaceutical not found with id: " + id));
    }

    @Transactional
    public PharmaceuticalDto create(PharmaceuticalRequest request) {
        log.info("Creating pharmaceutical: {}", request.getName());

        PharmaceuticalEntity entity = new PharmaceuticalEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setActive(request.getActive() != null ? request.getActive() : true);

        pharmaceuticalRepository.persist(entity);
        log.info("Pharmaceutical created with id: {}", entity.getId());

        return toDto.apply(entity);
    }

    @Transactional
    public PharmaceuticalDto update(UUID id, PharmaceuticalRequest request) {
        log.info("Updating pharmaceutical with id: {}", id);

        PharmaceuticalEntity entity = pharmaceuticalRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Pharmaceutical not found with id: " + id));

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }

        pharmaceuticalRepository.persist(entity);
        log.info("Pharmaceutical updated with id: {}", entity.getId());

        return toDto.apply(entity);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Soft deleting pharmaceutical with id: {}", id);

        PharmaceuticalEntity entity = pharmaceuticalRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Pharmaceutical not found with id: " + id));

        entity.setActive(false);
        pharmaceuticalRepository.persist(entity);
        log.info("Pharmaceutical soft deleted with id: {}", id);
    }

    public List<PharmaceuticalDto> getAllActive() {
        log.info("Fetching all active pharmaceuticals");
        return pharmaceuticalRepository.findAllActive()
                .stream()
                .map(toDto)
                .collect(Collectors.toList());
    }

    public static final Function<PharmaceuticalEntity, PharmaceuticalDto> toDto = entity ->
            PharmaceuticalDto.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .active(entity.getActive())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
}
