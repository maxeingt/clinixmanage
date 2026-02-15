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
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;
import org.apache.commons.lang3.*;

import java.util.*;
import java.util.function.*;

import static gt.com.xfactory.dto.response.PageResponse.toPageResponse;

@ApplicationScoped
@Slf4j
public class DiagnosisCatalogService {

    @Inject
    DiagnosisCatalogRepository diagnosisCatalogRepository;

    public PageResponse<DiagnosisCatalogDto> search(DiagnosisCatalogFilterDto filter, CommonPageRequest pageRequest) {
        log.info("Searching diagnosis catalog with filter: {}", filter.search);

        var fb = FilterBuilder.create()
                .addCondition(StringUtils.isNotBlank(filter.search),
                        "(LOWER(code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(name) LIKE LOWER(CONCAT('%', :search, '%')))",
                        "search", filter.search)
                .addLike(filter.code, "code");

        return toPageResponse(diagnosisCatalogRepository, fb.buildQuery(), pageRequest, fb.getParams(), toDto);
    }

    public DiagnosisCatalogDto getById(UUID id) {
        log.info("Fetching diagnosis catalog by id: {}", id);
        return diagnosisCatalogRepository.findByIdOptional(id)
                .map(toDto)
                .orElseThrow(() -> new NotFoundException("Diagnosis not found with id: " + id));
    }

    @Transactional
    public DiagnosisCatalogDto create(DiagnosisCatalogRequest request) {
        log.info("Creating diagnosis catalog: {} - {}", request.getCode(), request.getName());

        DiagnosisCatalogEntity entity = new DiagnosisCatalogEntity();
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setCategory(request.getCategory());
        entity.setChapter(request.getChapter());

        diagnosisCatalogRepository.persist(entity);
        log.info("Diagnosis catalog created with id: {}", entity.getId());

        return toDto.apply(entity);
    }

    @Transactional
    public DiagnosisCatalogDto update(UUID id, DiagnosisCatalogRequest request) {
        log.info("Updating diagnosis catalog: {}", id);

        DiagnosisCatalogEntity entity = diagnosisCatalogRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Diagnosis not found with id: " + id));

        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setCategory(request.getCategory());
        entity.setChapter(request.getChapter());

        diagnosisCatalogRepository.persist(entity);
        log.info("Diagnosis catalog updated: {}", id);

        return toDto.apply(entity);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting diagnosis catalog: {}", id);

        DiagnosisCatalogEntity entity = diagnosisCatalogRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Diagnosis not found with id: " + id));

        diagnosisCatalogRepository.delete(entity);
        log.info("Diagnosis catalog deleted: {}", id);
    }

    public static final Function<DiagnosisCatalogEntity, DiagnosisCatalogDto> toDto = entity ->
            DiagnosisCatalogDto.builder()
                    .id(entity.getId())
                    .code(entity.getCode())
                    .name(entity.getName())
                    .category(entity.getCategory())
                    .chapter(entity.getChapter())
                    .build();
}
