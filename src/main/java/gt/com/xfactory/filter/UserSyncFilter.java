package gt.com.xfactory.filter;

import gt.com.xfactory.entity.UserEntity;
import gt.com.xfactory.repository.UserRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Set;

/**
 * Filtro que sincroniza automáticamente usuarios de Keycloak a la BD local.
 * Se ejecuta después de la autenticación en cada request protegido.
 * Si el usuario no existe en la BD, lo crea automáticamente.
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 1)
@Slf4j
public class UserSyncFilter implements ContainerRequestFilter {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @Inject
    UserRepository userRepository;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Solo procesar si hay un usuario autenticado
        if (securityIdentity.isAnonymous()) {
            return;
        }

        try {
            syncUser();
        } catch (Exception e) {
            log.error("Error sincronizando usuario: {}", e.getMessage(), e);
            // No bloquear el request si falla la sincronización
        }
    }

    @Transactional
    void syncUser() {
        String keycloakId = jwt.getSubject();
        if (keycloakId == null || keycloakId.isBlank()) {
            log.warn("Token sin subject (keycloakId)");
            return;
        }

        // Verificar si el usuario ya existe
        if (userRepository.findByKeycloakId(keycloakId).isPresent()) {
            return; // Usuario ya existe, no hacer nada
        }

        // Extraer datos del token
        String username = jwt.getClaim("preferred_username");
        String email = jwt.getClaim("email");
        Set<String> roles = extractRealmRoles();

        // Crear nuevo usuario
        UserEntity user = new UserEntity();
        user.setKeycloakId(keycloakId);
        user.setUsername(username != null ? username : "user_" + keycloakId.substring(0, 8));
        user.setEmail(email != null ? email : username + "@placeholder.com");
        user.setRole(determineRole(roles));

        userRepository.persist(user);
        log.info("Usuario sincronizado automáticamente: {} con rol {}", username, user.getRole());
    }

    /**
     * Extrae los roles del realm desde el token JWT.
     * Los roles están en: realm_access.roles
     */
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

    /**
     * Determina el rol de la aplicación basado en los roles de Keycloak.
     * Prioridad: admin > doctor > secretary > user
     */
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
}
