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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Controlador de autenticación.
 * Maneja la sincronización de usuarios de Keycloak a la BD local.
 */
@RequestScoped
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin", "doctor", "secretary"})
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
        } else {
            // Usuario existe, sincronizar datos desde Keycloak
            String tokenEmail = jwt.getClaim("email");
            String tokenUsername = jwt.getClaim("preferred_username");

            if (tokenEmail != null && !tokenEmail.equals(user.getEmail())) {
                log.info("Sincronizando email de {} a {}", user.getEmail(), tokenEmail);
                user.setEmail(tokenEmail);
            }
            if (tokenUsername != null && !tokenUsername.equals(user.getUsername())) {
                log.info("Sincronizando username de {} a {}", user.getUsername(), tokenUsername);
                user.setUsername(tokenUsername);
            }

            String currentRole = determineRole(extractAllRoles());
            if (!currentRole.equals(user.getRole())) {
                log.info("Actualizando rol de {} de {} a {}", user.getUsername(), user.getRole(), currentRole);
                user.setRole(currentRole);
            }
        }

        return toDto(user);
    }

    private UserEntity createUserFromToken(String keycloakId) {
        String username = jwt.getClaim("preferred_username");
        String email = jwt.getClaim("email");
        Set<String> roles = extractAllRoles();

        UserEntity user = new UserEntity();
        user.setKeycloakId(keycloakId);
        user.setUsername(username != null ? username : "user_" + keycloakId.substring(0, 8));
        user.setEmail(email != null ? email : username + "@placeholder.com");
        user.setRole(determineRole(roles));

        userRepository.persist(user);
        return user;
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractAllRoles() {
        Set<String> allRoles = new HashSet<>();

        try {
            Object realmAccess = jwt.getClaim("realm_access");

            if (realmAccess instanceof jakarta.json.JsonObject) {
                jakarta.json.JsonObject jsonObj = (jakarta.json.JsonObject) realmAccess;
                jakarta.json.JsonArray rolesArray = jsonObj.getJsonArray("roles");
                if (rolesArray != null) {
                    for (int i = 0; i < rolesArray.size(); i++) {
                        allRoles.add(rolesArray.getString(i));
                    }
                }
            } else if (realmAccess instanceof Map) {
                Map<String, Object> realmAccessMap = (Map<String, Object>) realmAccess;
                Object roles = realmAccessMap.get("roles");

                if (roles instanceof jakarta.json.JsonArray) {
                    jakarta.json.JsonArray rolesArray = (jakarta.json.JsonArray) roles;
                    for (int i = 0; i < rolesArray.size(); i++) {
                        allRoles.add(rolesArray.getString(i));
                    }
                } else if (roles instanceof Collection) {
                    for (Object role : (Collection<?>) roles) {
                        if (role instanceof String) {
                            allRoles.add((String) role);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error extrayendo realm roles: ", e);
        }

        log.info("Roles extraídos del token: {}", allRoles);
        return allRoles;
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
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
