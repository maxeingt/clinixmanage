package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.repository.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.transaction.*;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;

import java.util.*;
import java.util.function.*;

@ApplicationScoped
@Slf4j
public class OrganizationService {

    @Inject
    OrganizationRepository organizationRepository;

    public PageResponse<OrganizationDto> getOrganizations(CommonPageRequest pageRequest) {
        log.info("Fetching organizations - pageRequest: {}", pageRequest);

        StringBuilder query = new StringBuilder("active = true");
        Map<String, Object> params = new HashMap<>();

        return PageResponse.toPageResponse(
                organizationRepository,
                query,
                pageRequest,
                params,
                toDto
        );
    }

    public OrganizationDto getById(UUID id) {
        log.info("Fetching organization by id: {}", id);

        OrganizationEntity entity = organizationRepository.findByIdOptional(id)
                .filter(OrganizationEntity::getActive)
                .orElseThrow(() ->
                        new NotFoundException("Organization not found with id: " + id));

        return toDto.apply(entity);
    }

    @Transactional
    public OrganizationDto create(OrganizationRequest request) {
        log.info("Creating organization with slug: {}", request.getSlug());

        validateSlugUnique(request.getSlug());

        OrganizationEntity entity = new OrganizationEntity();
        mapRequestToEntity(request, entity);

        organizationRepository.persist(entity);

        log.info("Organization created with id: {}", entity.getId());

        return toDto.apply(entity);
    }

    @Transactional
    public OrganizationDto update(UUID id, OrganizationRequest request) {
        log.info("Updating organization with id: {}", id);

        OrganizationEntity entity = organizationRepository.findByIdOptional(id)
                .orElseThrow(() ->
                        new NotFoundException("Organization not found with id: " + id));

        if (!entity.getSlug().equalsIgnoreCase(request.getSlug())) {
            validateSlugUnique(request.getSlug());
        }

        mapRequestToEntity(request, entity);

        return toDto.apply(entity);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Soft deleting organization with id: {}", id);

        OrganizationEntity entity = organizationRepository.findByIdOptional(id)
                .orElseThrow(() ->
                        new NotFoundException("Organization not found with id: " + id));

        entity.setActive(false);
    }

    private void validateSlugUnique(String slug) {
        organizationRepository.findBySlug(slug.toLowerCase().trim())
                .filter(OrganizationEntity::getActive)
                .ifPresent(existing -> {
                    throw new BadRequestException(
                            "Slug already exists: " + slug);
                });
    }

    private void mapRequestToEntity(OrganizationRequest request,
                                    OrganizationEntity entity) {
        entity.setName(request.getName());
        entity.setSlug(request.getSlug().toLowerCase().trim());
        entity.setLegalName(request.getLegalName());
        entity.setTaxId(request.getTaxId());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setAddress(request.getAddress());
        entity.setLogoUrl(request.getLogoUrl());
        entity.setSubscriptionPlan(request.getSubscriptionPlan());
        entity.setMaxUsers(request.getMaxUsers());

        if (entity.getActive() == null) {
            entity.setActive(true);
        }
    }

    public static final Function<OrganizationEntity, OrganizationDto> toDto =
            entity -> OrganizationDto.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .slug(entity.getSlug())
                    .legalName(entity.getLegalName())
                    .taxId(entity.getTaxId())
                    .email(entity.getEmail())
                    .phone(entity.getPhone())
                    .address(entity.getAddress())
                    .logoUrl(entity.getLogoUrl())
                    .active(entity.getActive())
                    .subscriptionPlan(entity.getSubscriptionPlan())
                    .maxUsers(entity.getMaxUsers())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
}
