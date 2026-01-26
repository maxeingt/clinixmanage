package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.dto.request.DistributorRequest;
import gt.com.xfactory.dto.request.filter.DistributorFilterDto;
import gt.com.xfactory.dto.response.DistributorDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.entity.DistributorEntity;
import gt.com.xfactory.repository.DistributorRepository;
import gt.com.xfactory.utils.QueryUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static gt.com.xfactory.dto.response.PageResponse.toPageResponse;

@ApplicationScoped
@Slf4j
public class DistributorService {

    @Inject
    DistributorRepository distributorRepository;

    public PageResponse<DistributorDto> getDistributors(DistributorFilterDto filter, @Valid CommonPageRequest pageRequest) {
        log.info("Fetching distributors with filter - pageRequest: {}, filter: {}", pageRequest, filter);

        StringBuilder query = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();

        QueryUtils.addLikeCondition(filter.name, "name", "name", conditions, params);

        if (filter.active != null) {
            conditions.add("active = :active");
            params.put("active", filter.active);
        }

        if (!conditions.isEmpty()) {
            query.append(String.join(" AND ", conditions));
        }

        return toPageResponse(distributorRepository, query, pageRequest, params, toDto);
    }

    public DistributorDto getById(UUID id) {
        log.info("Fetching distributor by id: {}", id);
        return distributorRepository.findByIdOptional(id)
                .map(toDto)
                .orElseThrow(() -> new NotFoundException("Distributor not found with id: " + id));
    }

    @Transactional
    public DistributorDto create(DistributorRequest request) {
        log.info("Creating distributor: {}", request.getName());

        DistributorEntity entity = new DistributorEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setActive(request.getActive() != null ? request.getActive() : true);

        distributorRepository.persist(entity);
        log.info("Distributor created with id: {}", entity.getId());

        return toDto.apply(entity);
    }

    @Transactional
    public DistributorDto update(UUID id, DistributorRequest request) {
        log.info("Updating distributor with id: {}", id);

        DistributorEntity entity = distributorRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Distributor not found with id: " + id));

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }

        distributorRepository.persist(entity);
        log.info("Distributor updated with id: {}", entity.getId());

        return toDto.apply(entity);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Soft deleting distributor with id: {}", id);

        DistributorEntity entity = distributorRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Distributor not found with id: " + id));

        entity.setActive(false);
        distributorRepository.persist(entity);
        log.info("Distributor soft deleted with id: {}", id);
    }

    public List<DistributorDto> getAllActive() {
        log.info("Fetching all active distributors");
        return distributorRepository.findAllActive()
                .stream()
                .map(toDto)
                .collect(Collectors.toList());
    }

    public static final Function<DistributorEntity, DistributorDto> toDto = entity ->
            DistributorDto.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .active(entity.getActive())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
}
