package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.UserRequest;
import gt.com.xfactory.dto.response.UserDto;
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
    DoctorRepository doctorRepository;

    @Inject
    KeycloakAdminService keycloakAdminService;

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

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        String role = request.getRole() != null ? request.getRole() : "user";

        // Crear usuario en Keycloak
        String keycloakId = keycloakAdminService.createUser(
                request.getUsername(), request.getEmail(), request.getPassword(), role);

        // Crear usuario en BD local
        UserEntity user = new UserEntity();
        user.setKeycloakId(keycloakId);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(role);

        userRepository.persist(user);
        log.info("User created with id: {} and keycloakId: {}", user.getId(), keycloakId);

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
    public UserDto toggleUserStatus(UUID id) {
        log.info("Toggling user status with id: {}", id);

        UserEntity user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        boolean newStatus = !Boolean.TRUE.equals(user.getActive());
        user.setActive(newStatus);

        if (newStatus) {
            keycloakAdminService.enableUser(user.getKeycloakId());
        } else {
            keycloakAdminService.disableUser(user.getKeycloakId());
        }

        log.info("User {} {}", id, newStatus ? "activado" : "desactivado");
        return toDto.apply(user);
    }

    // ============ Mappers ============

    public static final Function<UserEntity, UserDto> toDto = user -> UserDto.builder()
            .id(user.getId())
            .keycloakId(user.getKeycloakId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .active(user.getActive())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
}
