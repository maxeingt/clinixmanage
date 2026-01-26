package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.dto.request.MedicationRequest;
import gt.com.xfactory.dto.request.filter.MedicationFilterDto;
import gt.com.xfactory.dto.response.MedicationDto;
import gt.com.xfactory.dto.response.PageResponse;
import gt.com.xfactory.entity.DistributorEntity;
import gt.com.xfactory.entity.MedicationEntity;
import gt.com.xfactory.entity.PharmaceuticalEntity;
import gt.com.xfactory.repository.DistributorRepository;
import gt.com.xfactory.repository.MedicationRepository;
import gt.com.xfactory.repository.PharmaceuticalRepository;
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
public class MedicationService {

    @Inject
    MedicationRepository medicationRepository;

    @Inject
    PharmaceuticalRepository pharmaceuticalRepository;

    @Inject
    DistributorRepository distributorRepository;

    public PageResponse<MedicationDto> getMedications(MedicationFilterDto filter, @Valid CommonPageRequest pageRequest) {
        log.info("Fetching medications with filter - pageRequest: {}, filter: {}", pageRequest, filter);

        StringBuilder query = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();

        QueryUtils.addLikeCondition(filter.name, "name", "name", conditions, params);
        QueryUtils.addLikeCondition(filter.code, "code", "code", conditions, params);
        QueryUtils.addLikeCondition(filter.activeIngredient, "activeIngredient", "activeIngredient", conditions, params);

        if (filter.presentation != null) {
            conditions.add("presentation = :presentation");
            params.put("presentation", filter.presentation);
        }

        if (filter.pharmaceuticalId != null) {
            conditions.add("pharmaceutical.id = :pharmaceuticalId");
            params.put("pharmaceuticalId", filter.pharmaceuticalId);
        }

        if (filter.distributorId != null) {
            conditions.add("distributor.id = :distributorId");
            params.put("distributorId", filter.distributorId);
        }

        if (filter.active != null) {
            conditions.add("active = :active");
            params.put("active", filter.active);
        }

        if (!conditions.isEmpty()) {
            query.append(String.join(" AND ", conditions));
        }

        return toPageResponse(medicationRepository, query, pageRequest, params, toDto);
    }

    public MedicationDto getById(UUID id) {
        log.info("Fetching medication by id: {}", id);
        return medicationRepository.findByIdOptional(id)
                .map(toDto)
                .orElseThrow(() -> new NotFoundException("Medication not found with id: " + id));
    }

    @Transactional
    public MedicationDto create(MedicationRequest request) {
        log.info("Creating medication: {}", request.getName());

        MedicationEntity entity = new MedicationEntity();
        mapRequestToEntity(request, entity);

        medicationRepository.persist(entity);
        log.info("Medication created with id: {}", entity.getId());

        return toDto.apply(entity);
    }

    @Transactional
    public MedicationDto update(UUID id, MedicationRequest request) {
        log.info("Updating medication with id: {}", id);

        MedicationEntity entity = medicationRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Medication not found with id: " + id));

        mapRequestToEntity(request, entity);

        medicationRepository.persist(entity);
        log.info("Medication updated with id: {}", entity.getId());

        return toDto.apply(entity);
    }

    private void mapRequestToEntity(MedicationRequest request, MedicationEntity entity) {
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setCode(request.getCode());
        entity.setActiveIngredient(request.getActiveIngredient());
        entity.setConcentration(request.getConcentration());
        entity.setPresentation(request.getPresentation());
        entity.setIndications(request.getIndications());
        entity.setContraindications(request.getContraindications());
        entity.setPrice(request.getPrice());
        entity.setActive(request.getActive() != null ? request.getActive() : true);

        if (request.getPharmaceuticalId() != null) {
            PharmaceuticalEntity pharmaceutical = pharmaceuticalRepository.findByIdOptional(request.getPharmaceuticalId())
                    .orElseThrow(() -> new NotFoundException("Pharmaceutical not found with id: " + request.getPharmaceuticalId()));
            entity.setPharmaceutical(pharmaceutical);
        } else {
            entity.setPharmaceutical(null);
        }

        if (request.getDistributorId() != null) {
            DistributorEntity distributor = distributorRepository.findByIdOptional(request.getDistributorId())
                    .orElseThrow(() -> new NotFoundException("Distributor not found with id: " + request.getDistributorId()));
            entity.setDistributor(distributor);
        } else {
            entity.setDistributor(null);
        }
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Soft deleting medication with id: {}", id);

        MedicationEntity entity = medicationRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Medication not found with id: " + id));

        entity.setActive(false);
        medicationRepository.persist(entity);
        log.info("Medication soft deleted with id: {}", id);
    }

    public List<MedicationDto> getAllActive() {
        log.info("Fetching all active medications");
        return medicationRepository.findAllActive()
                .stream()
                .map(toDto)
                .collect(Collectors.toList());
    }

    public List<MedicationDto> searchByActiveIngredient(String activeIngredient) {
        log.info("Searching medications by active ingredient: {}", activeIngredient);
        return medicationRepository.findByActiveIngredient(activeIngredient)
                .stream()
                .filter(m -> m.getActive())
                .map(toDto)
                .collect(Collectors.toList());
    }

    public static final Function<MedicationEntity, MedicationDto> toDto = entity ->
            MedicationDto.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .code(entity.getCode())
                    .activeIngredient(entity.getActiveIngredient())
                    .concentration(entity.getConcentration())
                    .presentation(entity.getPresentation())
                    .presentationDisplay(entity.getPresentation() != null ? entity.getPresentation().name() : null)
                    .indications(entity.getIndications())
                    .contraindications(entity.getContraindications())
                    .price(entity.getPrice())
                    .active(entity.getActive())
                    .pharmaceuticalId(entity.getPharmaceutical() != null ? entity.getPharmaceutical().getId() : null)
                    .pharmaceuticalName(entity.getPharmaceutical() != null ? entity.getPharmaceutical().getName() : null)
                    .distributorId(entity.getDistributor() != null ? entity.getDistributor().getId() : null)
                    .distributorName(entity.getDistributor() != null ? entity.getDistributor().getName() : null)
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
}
