package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.AssignClinicPermissionRequest;
import gt.com.xfactory.dto.request.UserRequest;
import gt.com.xfactory.dto.response.UserClinicPermissionDto;
import gt.com.xfactory.dto.response.UserDto;
import gt.com.xfactory.entity.ClinicEntity;
import gt.com.xfactory.entity.RoleTemplateEntity;
import gt.com.xfactory.entity.UserClinicPermissionEntity;
import gt.com.xfactory.entity.UserClinicPermissionId;
import gt.com.xfactory.entity.UserEntity;
import gt.com.xfactory.repository.*;
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
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    UserClinicPermissionRepository userClinicPermissionRepository;

    @Inject
    ClinicRepository clinicRepository;

    @Inject
    RoleTemplateRepository roleTemplateRepository;

    @Inject
    DoctorRepository doctorRepository;

    public List<UserDto> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.listAll().stream()
                .map(toDto)
                .toList();
    }

    public UserDto getUserById(UUID id) {
        log.info("Fetching user by id: {}", id);
        return userRepository.findByIdOptional(id)
                .map(toDto)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    public UserDto getUserByKeycloakId(String keycloakId) {
        log.info("Fetching user by keycloakId: {}", keycloakId);
        return userRepository.findByKeycloakId(keycloakId)
                .map(toDto)
                .orElseThrow(() -> new NotFoundException("User not found with keycloakId: " + keycloakId));
    }

    @Transactional
    public UserDto createUser(UserRequest request) {
        log.info("Creating user with username: {}", request.getUsername());

        UserEntity user = new UserEntity();
        user.setKeycloakId(request.getKeycloakId());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole() != null ? request.getRole() : "user");

        userRepository.persist(user);
        log.info("User created with id: {}", user.getId());

        return toDto.apply(user);
    }

    @Transactional
    public UserDto updateUser(UUID id, UserRequest request) {
        log.info("Updating user with id: {}", id);

        UserEntity user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        String oldEmail = user.getEmail();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        userRepository.persist(user);

        // Sincronizar email en doctor vinculado
        if (request.getEmail() != null && !request.getEmail().equals(oldEmail)) {
            doctorRepository.find("user.id", id).firstResultOptional()
                    .ifPresent(doctor -> doctor.setEmail(request.getEmail()));
        }

        return toDto.apply(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user with id: {}", id);

        UserEntity user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        userRepository.delete(user);
    }

    // ============ Clinic Permissions ============

    public List<UserClinicPermissionDto> getUserClinicPermissions(UUID userId) {
        log.info("Fetching clinic permissions for user: {}", userId);

        // Verify user exists
        userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        return userClinicPermissionRepository.findByUserId(userId).stream()
                .map(toPermissionDto)
                .toList();
    }

    @Transactional
    public UserClinicPermissionDto assignClinicPermission(UUID userId, AssignClinicPermissionRequest request) {
        log.info("Assigning clinic {} to user {}", request.getClinicId(), userId);

        UserEntity user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        ClinicEntity clinic = clinicRepository.findByIdOptional(request.getClinicId())
                .orElseThrow(() -> new NotFoundException("Clinic not found with id: " + request.getClinicId()));

        // Check if permission already exists
        var existingPermission = userClinicPermissionRepository
                .findByUserIdAndClinicId(userId, request.getClinicId());

        UserClinicPermissionEntity permission;
        if (existingPermission.isPresent()) {
            // Update existing permission
            permission = existingPermission.get();
            log.info("Updating existing permission for user {} and clinic {}", userId, request.getClinicId());
        } else {
            // Create new permission
            permission = new UserClinicPermissionEntity();
            permission.setId(new UserClinicPermissionId(userId, request.getClinicId()));
            permission.setUser(user);
            permission.setClinic(clinic);
        }

        // Si se proporciona un roleTemplateId, usar los permisos de la plantilla
        if (request.getRoleTemplateId() != null) {
            RoleTemplateEntity roleTemplate = roleTemplateRepository.findByIdOptional(request.getRoleTemplateId())
                    .orElseThrow(() -> new NotFoundException("Role template not found with id: " + request.getRoleTemplateId()));

            permission.setRoleTemplate(roleTemplate);
            permission.setAdminPatients(roleTemplate.getAdminPatients());
            permission.setAdminDoctors(roleTemplate.getAdminDoctors());
            permission.setAdminAppointments(roleTemplate.getAdminAppointments());
            permission.setAdminClinics(roleTemplate.getAdminClinics());
            permission.setAdminUsers(roleTemplate.getAdminUsers());
            permission.setAdminSpecialties(roleTemplate.getAdminSpecialties());
            permission.setManageAssignments(roleTemplate.getManageAssignments());
            permission.setViewMedicalRecords(roleTemplate.getViewMedicalRecords());
            log.info("Applying role template: {}", roleTemplate.getName());
        } else {
            // Usar permisos individuales del request
            permission.setAdminPatients(request.getAdminPatients());
            permission.setAdminDoctors(request.getAdminDoctors());
            permission.setAdminAppointments(request.getAdminAppointments());
            permission.setAdminClinics(request.getAdminClinics());
            permission.setAdminUsers(request.getAdminUsers());
            permission.setAdminSpecialties(request.getAdminSpecialties());
            permission.setManageAssignments(request.getManageAssignments());
            permission.setViewMedicalRecords(request.getViewMedicalRecords());
        }

        userClinicPermissionRepository.persist(permission);
        log.info("Clinic permission assigned successfully");

        return toPermissionDto.apply(permission);
    }

    @Transactional
    public void revokeClinicPermission(UUID userId, UUID clinicId) {
        log.info("Revoking clinic {} permission from user {}", clinicId, userId);

        UserClinicPermissionEntity permission = userClinicPermissionRepository
                .findByUserIdAndClinicId(userId, clinicId)
                .orElseThrow(() -> new NotFoundException(
                        "Permission not found for user " + userId + " and clinic " + clinicId));

        userClinicPermissionRepository.delete(permission);
        log.info("Clinic permission revoked successfully");
    }

    public boolean hasAccessToClinic(UUID userId, UUID clinicId) {
        return userClinicPermissionRepository.hasAccessToClinic(userId, clinicId);
    }

    // ============ Mappers ============

    public static final Function<UserEntity, UserDto> toDto = user -> UserDto.builder()
            .id(user.getId())
            .keycloakId(user.getKeycloakId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();

    public static final Function<UserClinicPermissionEntity, UserClinicPermissionDto> toPermissionDto =
            permission -> UserClinicPermissionDto.builder()
                    .userId(permission.getId().getUserId())
                    .clinicId(permission.getId().getClinicId())
                    .clinicName(permission.getClinic() != null ? permission.getClinic().getName() : null)
                    .adminPatients(permission.getAdminPatients())
                    .adminDoctors(permission.getAdminDoctors())
                    .adminAppointments(permission.getAdminAppointments())
                    .adminClinics(permission.getAdminClinics())
                    .adminUsers(permission.getAdminUsers())
                    .adminSpecialties(permission.getAdminSpecialties())
                    .manageAssignments(permission.getManageAssignments())
                    .viewMedicalRecords(permission.getViewMedicalRecords())
                    .roleTemplateId(permission.getRoleTemplate() != null ? permission.getRoleTemplate().getId() : null)
                    .roleTemplateName(permission.getRoleTemplate() != null ? permission.getRoleTemplate().getName() : null)
                    .createdAt(permission.getCreatedAt())
                    .build();
}
