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

    @Inject
    SecurityContextService securityContextService;

    @Inject
    OrganizationRepository organizationRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.listAll().stream()
                .map(toDto)
                .toList();
    }

    public PageResponse<UserDto> getUsersPaginated(UserFilterDto filter, @Valid CommonPageRequest pageRequest) {
        var fb = FilterBuilder.create()
                .addLike(filter.username, "username")
                .addLike(filter.email, "email")
                .addEquals(filter.role, "role");

        return toPageResponse(userRepository, fb.buildQuery(), pageRequest, fb.getParams(), toDto);
    }

    public UserDto getUserById(UUID id) {
        return userRepository.findByIdOptional(id)
                .map(toDto)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    public UserDto getUserByKeycloakId(String keycloakId) {
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

        // 🔐 Obtener contexto del JWT
        String organizationId = securityContextService.getCurrentOrganizationId().toString();
        String organizationSlug = securityContextService.getCurrentOrganizationSlug();

        String role = request.getRole() != null ? request.getRole() : "user";

        // Crear usuario en Keycloak
        UserEntity user = buildAndPersistUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                role,
                organizationId,
                organizationSlug
        );

        log.info("User created with id: {} and keycloakId: {}", user.getId(), user.getKeycloakId());

        return toDto.apply(user);
    }

    @Transactional
    public UserDto updateUser(UUID id, UserRequest request) {
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
        UserEntity user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        keycloakAdminService.changePassword(user.getKeycloakId(), oldPassword, newPassword);
    }

    @Transactional
    public UserDto createAdminForOrganization(UUID orgId, AdminRequest request) {

        log.info("Creating ADMIN for organization: {}", orgId);

        // 1. Validar que la organización exista y esté activa (native query para bypasear @TenantId)
        @SuppressWarnings("unchecked")
        List<Object[]> orgResults = userRepository.getEntityManager()
                .createNativeQuery("SELECT id, slug FROM organization WHERE id = :id AND active = true")
                .setParameter("id", orgId.toString())
                .getResultList();

        if (orgResults.isEmpty()) {
            throw new NotFoundException("Organization not found with id: " + orgId);
        }

        Object[] orgRow = orgResults.get(0);
        UUID organizationId = orgRow[0] instanceof UUID ? (UUID) orgRow[0] : UUID.fromString(orgRow[0].toString());
        String organizationSlug = (String) orgRow[1];

        // 2. Validar unicidad global (username y email son únicos en Keycloak realm)
        validateGlobalUniqueness(request.getUsername(), request.getEmail());

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        UserEntity user = buildAndPersistUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                "admin",
                organizationId.toString(),
                organizationSlug
        );

        log.info("Admin created with id: {} for organization: {}", user.getId(), orgId);

        return toDto.apply(user);
    }

    private void validateGlobalUniqueness(String username, String email) {
        // Native query para evitar el filtro automático de @TenantId
        long usernameCount = (long) userRepository.getEntityManager()
                .createNativeQuery("SELECT COUNT(*) FROM \"user\" WHERE username = :username")
                .setParameter("username", username)
                .getSingleResult();

        if (usernameCount > 0) {
            throw new BadRequestException("Username already exists");
        }

        long emailCount = (long) userRepository.getEntityManager()
                .createNativeQuery("SELECT COUNT(*) FROM \"user\" WHERE email = :email")
                .setParameter("email", email)
                .getSingleResult();

        if (emailCount > 0) {
            throw new BadRequestException("Email already exists");
        }
    }

    private UserEntity buildAndPersistUser(
            String username,
            String email,
            String password,
            String firstName,
            String lastName,
            String role,
            String organizationId,
            String organizationSlug
    ) {

        // Crear en Keycloak
        String keycloakId = keycloakAdminService.createUser(
                username, email, password, role, organizationId, organizationSlug);

        UserEntity user = new UserEntity();
        user.setKeycloakId(keycloakId);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRole(role);
        user.setActive(true);

        userRepository.persist(user);

        return user;
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
