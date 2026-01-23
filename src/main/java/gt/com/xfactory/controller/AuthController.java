package gt.com.xfactory.controller;

import gt.com.xfactory.dto.response.UserDto;
import gt.com.xfactory.entity.UserEntity;
import gt.com.xfactory.repository.UserRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Set;

/**
 * Controlador de autenticación.
 * Maneja la sincronización de usuarios de Keycloak a la BD local.
 */
@RequestScoped
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
@Slf4j
public class AuthController {

    @Inject
    JsonWebToken jwt;

    @Inject
    UserRepository userRepository;

    /**
     * Obtiene el usuario actual.
     * Si no existe en la BD, lo crea automáticamente basado en el token de Keycloak.
     * El frontend debe llamar este endpoint una vez después del login.
     */
    @GET
    @Path("/me")
    @Transactional
    public UserDto me() {
        String keycloakId = jwt.getSubject();
        log.info("GET /auth/me - keycloakId: {}", keycloakId);

        // Buscar usuario existente
        UserEntity user = userRepository.findByKeycloakId(keycloakId).orElse(null);

        if (user == null) {
            // Usuario no existe, crear
            user = createUserFromToken(keycloakId);
            log.info("Usuario creado: {} con rol {}", user.getUsername(), user.getRole());
        }

        return toDto(user);
    }

    private UserEntity createUserFromToken(String keycloakId) {
        String username = jwt.getClaim("preferred_username");
        String email = jwt.getClaim("email");
        Set<String> roles = extractRealmRoles();

        UserEntity user = new UserEntity();
        user.setKeycloakId(keycloakId);
        user.setUsername(username != null ? username : "user_" + keycloakId.substring(0, 8));
        user.setEmail(email != null ? email : username + "@placeholder.com");
        user.setRole(determineRole(roles));

        userRepository.persist(user);
        return user;
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRealmRoles() {
        try {
            Object realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof java.util.Map) {
                java.util.Map<String, Object> realmAccessMap = (java.util.Map<String, Object>) realmAccess;
                Object roles = realmAccessMap.get("roles");
                if (roles instanceof java.util.Collection) {
                    return new java.util.HashSet<>((java.util.Collection<String>) roles);
                }
            }
        } catch (Exception e) {
            log.warn("No se pudieron extraer roles del token: {}", e.getMessage());
        }
        return Set.of();
    }

    private String determineRole(Set<String> keycloakRoles) {
        if (keycloakRoles.contains("admin")) {
            return "ADMIN";
        }
        if (keycloakRoles.contains("doctor")) {
            return "DOCTOR";
        }
        if (keycloakRoles.contains("secretary")) {
            return "SECRETARY";
        }
        return "USER";
    }

    private UserDto toDto(UserEntity user) {
        return UserDto.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
