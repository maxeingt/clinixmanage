package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.repository.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.transaction.*;
import lombok.extern.slf4j.*;
import org.eclipse.microprofile.jwt.*;

import java.util.*;

@ApplicationScoped
@Slf4j
public class AuthService {

    @Inject
    JsonWebToken jwt;

    @Inject
    UserRepository userRepository;

    @Transactional
    public UserDto syncCurrentUser() {
        String keycloakId = jwt.getSubject();
        log.info("Syncing user - keycloakId: {}", keycloakId);

        UserEntity user = userRepository.findByKeycloakId(keycloakId).orElse(null);

        if (user == null) {
            user = createUserFromToken(keycloakId);
            log.info("Usuario creado: {} con rol {}", user.getUsername(), user.getRole());
        } else {
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

        return UserService.toDto.apply(user);
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
}
