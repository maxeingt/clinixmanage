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
import java.util.stream.*;

@ApplicationScoped
@Slf4j
public class SpecialtyFormTemplateService {

    @Inject
    SpecialtyFormTemplateRepository templateRepository;

    @Inject
    SpecialtyRepository specialtyRepository;

    public List<SpecialtyFormTemplateDto> getAllActive() {
        return templateRepository.findAllActive().stream()
                .map(toDto)
                .collect(Collectors.toList());
    }

    public List<SpecialtyFormTemplateDto> getBySpecialtyId(UUID specialtyId) {
        return templateRepository.findAllBySpecialtyId(specialtyId).stream()
                .map(toDto)
                .collect(Collectors.toList());
    }

    public List<SpecialtyFormTemplateDto> getActiveBySpecialtyId(UUID specialtyId) {
        return templateRepository.findActiveBySpecialtyId(specialtyId).stream()
                .map(toDto)
                .collect(Collectors.toList());
    }

    public SpecialtyFormTemplateDto getById(UUID id) {
        return templateRepository.findByIdOptional(id)
                .map(toDto)
                .orElseThrow(() -> new NotFoundException("Form template no encontrado con id: " + id));
    }

    @Transactional
    public SpecialtyFormTemplateDto create(SpecialtyFormTemplateRequest request) {
        log.info("Creando form template: {} para especialidad: {}", request.getFormName(), request.getSpecialtyId());

        var specialty = specialtyRepository.findByIdOptional(request.getSpecialtyId())
                .orElseThrow(() -> new NotFoundException("Especialidad no encontrada con id: " + request.getSpecialtyId()));

        SpecialtyFormTemplateEntity entity = new SpecialtyFormTemplateEntity();
        entity.setSpecialty(specialty);
        entity.setFormName(request.getFormName());
        entity.setDescription(request.getDescription());
        entity.setFormSchema(request.getFormSchema());
        entity.setIsActive(true);
        entity.setVersion(1);

        templateRepository.persist(entity);
        log.info("Form template creado con id: {}", entity.getId());

        return toDto.apply(entity);
    }

    @Transactional
    public SpecialtyFormTemplateDto update(UUID id, SpecialtyFormTemplateRequest request) {
        log.info("Actualizando form template: {}", id);

        var entity = templateRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Form template no encontrado con id: " + id));

        if (request.getFormName() != null) entity.setFormName(request.getFormName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getFormSchema() != null) entity.setFormSchema(request.getFormSchema());

        templateRepository.persist(entity);
        log.info("Form template actualizado: {}", id);

        return toDto.apply(entity);
    }

    @Transactional
    public SpecialtyFormTemplateDto toggleStatus(UUID id) {
        log.info("Cambiando estado de form template: {}", id);

        var entity = templateRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Form template no encontrado con id: " + id));

        boolean newStatus = !entity.getIsActive();

        if (newStatus) {
            templateRepository.deactivateBySpecialtyAndFormName(
                    entity.getSpecialty().getId(), entity.getFormName());
        }

        entity.setIsActive(newStatus);
        templateRepository.persist(entity);
        log.info("Form template {} ahora está {}", id, newStatus ? "activo" : "inactivo");

        return toDto.apply(entity);
    }

    @Transactional
    public SpecialtyFormTemplateDto duplicate(UUID id) {
        log.info("Duplicando form template: {}", id);

        var original = templateRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Form template no encontrado con id: " + id));

        int nextVersion = templateRepository.findMaxVersion(
                original.getSpecialty().getId(), original.getFormName())
                .orElse(0) + 1;

        templateRepository.deactivateBySpecialtyAndFormName(
                original.getSpecialty().getId(), original.getFormName());

        SpecialtyFormTemplateEntity copy = new SpecialtyFormTemplateEntity();
        copy.setSpecialty(original.getSpecialty());
        copy.setFormName(original.getFormName());
        copy.setDescription(original.getDescription());
        copy.setFormSchema(new HashMap<>(original.getFormSchema()));
        copy.setIsActive(true);
        copy.setVersion(nextVersion);

        templateRepository.persist(copy);
        log.info("Form template duplicado como versión {} con id: {}", nextVersion, copy.getId());

        return toDto.apply(copy);
    }

    public static final Function<SpecialtyFormTemplateEntity, SpecialtyFormTemplateDto> toDto = entity ->
            SpecialtyFormTemplateDto.builder()
                    .id(entity.getId())
                    .specialtyId(entity.getSpecialty().getId())
                    .specialtyName(entity.getSpecialty().getName())
                    .formName(entity.getFormName())
                    .description(entity.getDescription())
                    .formSchema(entity.getFormSchema())
                    .isActive(entity.getIsActive())
                    .version(entity.getVersion())
                    .build();
}
