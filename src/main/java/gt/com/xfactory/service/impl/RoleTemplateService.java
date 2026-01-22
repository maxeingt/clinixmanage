package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.response.RoleTemplateDto;
import gt.com.xfactory.entity.RoleTemplateEntity;
import gt.com.xfactory.repository.RoleTemplateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@ApplicationScoped
@Slf4j
public class RoleTemplateService {

    @Inject
    RoleTemplateRepository roleTemplateRepository;

    public List<RoleTemplateDto> getAllRoleTemplates() {
        log.info("Fetching all role templates");
        return roleTemplateRepository.listAll().stream()
                .map(toDto)
                .toList();
    }

    public RoleTemplateDto getRoleTemplateById(UUID id) {
        log.info("Fetching role template by id: {}", id);
        RoleTemplateEntity entity = roleTemplateRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Role template not found with id: " + id));
        return toDto.apply(entity);
    }

    public RoleTemplateDto getRoleTemplateByName(String name) {
        log.info("Fetching role template by name: {}", name);
        RoleTemplateEntity entity = roleTemplateRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Role template not found with name: " + name));
        return toDto.apply(entity);
    }

    public RoleTemplateEntity getEntityById(UUID id) {
        return roleTemplateRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Role template not found with id: " + id));
    }

    public RoleTemplateEntity getEntityByName(String name) {
        return roleTemplateRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Role template not found with name: " + name));
    }

    @Transactional
    public void initializeDefaultRoles() {
        log.info("Initializing default role templates");

        createRoleIfNotExists("ADMIN", "Administrador del sistema",
                true, true, true, true, true, true, true, true);

        createRoleIfNotExists("DOCTOR", "Médico",
                true, false, true, false, false, false, false, true);

        createRoleIfNotExists("SECRETARY", "Secretaria/Recepcionista",
                true, false, true, false, false, false, false, false);

        log.info("Default role templates initialized");
    }

    private void createRoleIfNotExists(String name, String description,
                                       boolean adminPatients, boolean adminDoctors,
                                       boolean adminAppointments, boolean adminClinics,
                                       boolean adminUsers, boolean adminSpecialties,
                                       boolean manageAssignments, boolean viewMedicalRecords) {
        if (roleTemplateRepository.findByName(name).isEmpty()) {
            RoleTemplateEntity role = new RoleTemplateEntity();
            role.setName(name);
            role.setDescription(description);
            role.setAdminPatients(adminPatients);
            role.setAdminDoctors(adminDoctors);
            role.setAdminAppointments(adminAppointments);
            role.setAdminClinics(adminClinics);
            role.setAdminUsers(adminUsers);
            role.setAdminSpecialties(adminSpecialties);
            role.setManageAssignments(manageAssignments);
            role.setViewMedicalRecords(viewMedicalRecords);
            roleTemplateRepository.persist(role);
            log.info("Created role template: {}", name);
        }
    }

    public static final Function<RoleTemplateEntity, RoleTemplateDto> toDto = entity ->
            RoleTemplateDto.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .adminPatients(entity.getAdminPatients())
                    .adminDoctors(entity.getAdminDoctors())
                    .adminAppointments(entity.getAdminAppointments())
                    .adminClinics(entity.getAdminClinics())
                    .adminUsers(entity.getAdminUsers())
                    .adminSpecialties(entity.getAdminSpecialties())
                    .manageAssignments(entity.getManageAssignments())
                    .viewMedicalRecords(entity.getViewMedicalRecords())
                    .build();
}
