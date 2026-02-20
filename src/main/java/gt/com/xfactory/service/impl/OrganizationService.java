package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.repository.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.persistence.*;
import jakarta.transaction.*;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

@ApplicationScoped
@Slf4j
public class OrganizationService {

    @Inject
    OrganizationRepository organizationRepository;

    // Native queries sin mapeo a entidad para bypasear el filtro DISCRIMINATOR de @TenantId,
    // ya que OrganizationEntity no tiene @TenantId (las organizaciones SON los tenants).
    private EntityManager em() {
        return organizationRepository.getEntityManager();
    }

    public PageResponse<OrganizationDto> getOrganizations(CommonPageRequest pageRequest) {
        long totalItems = ((Number) em()
                .createNativeQuery("SELECT COUNT(*) FROM organization WHERE active = true")
                .getSingleResult()).longValue();

        int totalPages = (int) Math.ceil((double) totalItems / pageRequest.getSize());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em()
                .createNativeQuery("SELECT id, name, slug, legal_name, tax_id, email, phone, address, logo_url, active, subscription_plan, max_users, created_at, updated_at FROM organization WHERE active = true ORDER BY created_at DESC")
                .setFirstResult(pageRequest.getPage() * pageRequest.getSize())
                .setMaxResults(pageRequest.getSize())
                .getResultList();

        List<OrganizationDto> content = rows.stream()
                .map(this::mapRowToDto)
                .collect(Collectors.toList());

        return new PageResponse<>(content, pageRequest.getPage(), totalPages, totalItems);
    }

    public OrganizationDto getById(UUID id) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em()
                .createNativeQuery("SELECT id, name, slug, legal_name, tax_id, email, phone, address, logo_url, active, subscription_plan, max_users, created_at, updated_at FROM organization WHERE id = :id AND active = true")
                .setParameter("id", id.toString())
                .getResultList();

        return rows.stream().findFirst()
                .map(this::mapRowToDto)
                .orElseThrow(() -> new NotFoundException("Organization not found with id: " + id));
    }

    @Transactional
    public OrganizationDto create(OrganizationRequest request) {
        log.info("Creating organization with slug: {}", request.getSlug());

        validateSlugUnique(request.getSlug(), null);

        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        em().createNativeQuery("INSERT INTO organization (id, name, slug, legal_name, tax_id, email, phone, address, logo_url, active, subscription_plan, max_users, created_at) VALUES (:id, :name, :slug, :legalName, :taxId, :email, :phone, :address, :logoUrl, true, :subscriptionPlan, :maxUsers, :createdAt)")
                .setParameter("id", id.toString())
                .setParameter("name", request.getName())
                .setParameter("slug", request.getSlug().toLowerCase().trim())
                .setParameter("legalName", request.getLegalName())
                .setParameter("taxId", request.getTaxId())
                .setParameter("email", request.getEmail())
                .setParameter("phone", request.getPhone())
                .setParameter("address", request.getAddress())
                .setParameter("logoUrl", request.getLogoUrl())
                .setParameter("subscriptionPlan", request.getSubscriptionPlan())
                .setParameter("maxUsers", request.getMaxUsers())
                .setParameter("createdAt", now)
                .executeUpdate();

        log.info("Organization created with id: {}", id);

        return getById(id);
    }

    @Transactional
    public OrganizationDto update(UUID id, OrganizationRequest request) {
        OrganizationDto existing = getById(id);

        if (!existing.getSlug().equalsIgnoreCase(request.getSlug())) {
            validateSlugUnique(request.getSlug(), id);
        }

        em().createNativeQuery("UPDATE organization SET name = :name, slug = :slug, legal_name = :legalName, tax_id = :taxId, email = :email, phone = :phone, address = :address, logo_url = :logoUrl, subscription_plan = :subscriptionPlan, max_users = :maxUsers, updated_at = :updatedAt WHERE id = :id")
                .setParameter("id", id.toString())
                .setParameter("name", request.getName())
                .setParameter("slug", request.getSlug().toLowerCase().trim())
                .setParameter("legalName", request.getLegalName())
                .setParameter("taxId", request.getTaxId())
                .setParameter("email", request.getEmail())
                .setParameter("phone", request.getPhone())
                .setParameter("address", request.getAddress())
                .setParameter("logoUrl", request.getLogoUrl())
                .setParameter("subscriptionPlan", request.getSubscriptionPlan())
                .setParameter("maxUsers", request.getMaxUsers())
                .setParameter("updatedAt", LocalDateTime.now())
                .executeUpdate();

        return getById(id);
    }

    @Transactional
    public void delete(UUID id) {
        int updated = em().createNativeQuery("UPDATE organization SET active = false, updated_at = :updatedAt WHERE id = :id")
                .setParameter("id", id.toString())
                .setParameter("updatedAt", LocalDateTime.now())
                .executeUpdate();

        if (updated == 0) {
            throw new NotFoundException("Organization not found with id: " + id);
        }
    }

    private void validateSlugUnique(String slug, UUID excludeId) {
        String sql = "SELECT COUNT(*) FROM organization WHERE LOWER(slug) = :slug AND active = true"
                + (excludeId != null ? " AND id != :excludeId" : "");

        var query = em().createNativeQuery(sql)
                .setParameter("slug", slug.toLowerCase().trim());

        if (excludeId != null) {
            query.setParameter("excludeId", excludeId.toString());
        }

        long count = ((Number) query.getSingleResult()).longValue();
        if (count > 0) {
            throw new BadRequestException("Slug already exists: " + slug);
        }
    }

    private OrganizationDto mapRowToDto(Object[] row) {
        return OrganizationDto.builder()
                .id(toUUID(row[0]))
                .name((String) row[1])
                .slug((String) row[2])
                .legalName((String) row[3])
                .taxId((String) row[4])
                .email((String) row[5])
                .phone((String) row[6])
                .address((String) row[7])
                .logoUrl((String) row[8])
                .active((Boolean) row[9])
                .subscriptionPlan((String) row[10])
                .maxUsers(row[11] != null ? ((Number) row[11]).intValue() : null)
                .createdAt(toLocalDateTime(row[12]))
                .updatedAt(toLocalDateTime(row[13]))
                .build();
    }

    private UUID toUUID(Object value) {
        if (value == null) return null;
        if (value instanceof UUID) return (UUID) value;
        return UUID.fromString(value.toString());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        if (value instanceof Timestamp) return ((Timestamp) value).toLocalDateTime();
        return LocalDateTime.parse(value.toString());
    }
}
