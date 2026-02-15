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

import static gt.com.xfactory.dto.response.PageResponse.toPageResponse;

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

    public PageResponse<UserDto> getUsersPaginated(UserFilterDto filter, @Valid CommonPageRequest pageRequest) {
        log.info("Fetching users with filter - pageRequest: {}, filter: {}", pageRequest, filter);

        var fb = FilterBuilder.create()
                .addLike(filter.username, "username")
                .addLike(filter.email, "email")
                .addEquals(filter.role, "role");

        return toPageResponse(userRepository, fb.buildQuery(), pageRequest, fb.getParams(), toDto);
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
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
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
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
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

    public void changePassword(UUID id, String oldPassword, String newPassword) {
        log.info("Changing password for user with id: {}", id);

        UserEntity user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        keycloakAdminService.changePassword(user.getKeycloakId(), oldPassword, newPassword);
    }

    // ============ Mappers ============

    public static final Function<UserEntity, UserDto> toDto = user -> UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .role(user.getRole())
            .active(user.getActive())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
}
