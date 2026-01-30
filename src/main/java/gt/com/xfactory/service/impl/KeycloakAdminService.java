package gt.com.xfactory.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
@Slf4j
public class KeycloakAdminService {

    @ConfigProperty(name = "keycloak.admin.server-url")
    String serverUrl;

    @ConfigProperty(name = "keycloak.admin.realm")
    String realm;

    @ConfigProperty(name = "keycloak.admin.client-id")
    String clientId;

    @ConfigProperty(name = "keycloak.admin.client-secret")
    String clientSecret;

    private Keycloak keycloak;

    @PostConstruct
    void init() {
        keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType("client_credentials")
                .build();
    }

    @PreDestroy
    void close() {
        if (keycloak != null) {
            keycloak.close();
        }
    }

    private RealmResource getRealmResource() {
        return keycloak.realm(realm);
    }

    private UsersResource getUsersResource() {
        return getRealmResource().users();
    }

    /**
     * Crea un usuario en Keycloak y retorna su ID.
     */
    public String createUser(String username, String email, String password, String role) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(Collections.singletonList(credential));

        try (Response response = getUsersResource().create(user)) {
            if (response.getStatus() == 201) {
                String locationHeader = response.getHeaderString("Location");
                String keycloakId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
                log.info("Usuario creado en Keycloak con id: {}", keycloakId);

                try {
                    assignRealmRole(keycloakId, role);
                } catch (Exception e) {
                    // Rollback: eliminar usuario de Keycloak si falla la asignación de rol
                    getUsersResource().delete(keycloakId);
                    log.error("Rollback: usuario {} eliminado de Keycloak por fallo en asignación de rol", keycloakId);
                    throw e;
                }

                return keycloakId;
            } else if (response.getStatus() == 409) {
                throw new IllegalStateException("El usuario ya existe en Keycloak con ese username o email");
            } else {
                throw new IllegalStateException("Error creando usuario en Keycloak: HTTP " + response.getStatus());
            }
        }
    }

    /**
     * Desactiva un usuario en Keycloak.
     */
    public void disableUser(String keycloakId) {
        try {
            UserRepresentation user = getUsersResource().get(keycloakId).toRepresentation();
            user.setEnabled(false);
            getUsersResource().get(keycloakId).update(user);
            log.info("Usuario desactivado en Keycloak: {}", keycloakId);
        } catch (Exception e) {
            log.error("Error desactivando usuario en Keycloak: {}", keycloakId, e);
            throw new IllegalStateException("Error desactivando usuario en Keycloak", e);
        }
    }

    /**
     * Activa un usuario en Keycloak.
     */
    public void enableUser(String keycloakId) {
        try {
            UserRepresentation user = getUsersResource().get(keycloakId).toRepresentation();
            user.setEnabled(true);
            getUsersResource().get(keycloakId).update(user);
            log.info("Usuario activado en Keycloak: {}", keycloakId);
        } catch (Exception e) {
            log.error("Error activando usuario en Keycloak: {}", keycloakId, e);
            throw new IllegalStateException("Error activando usuario en Keycloak", e);
        }
    }

    private void assignRealmRole(String keycloakId, String role) {
        if (role == null || role.isBlank()) {
            role = "user";
        }

        String roleName = role.toLowerCase();

        try {
            RoleRepresentation realmRole = getRealmResource().roles().get(roleName).toRepresentation();
            getUsersResource().get(keycloakId).roles().realmLevel().add(List.of(realmRole));
            log.info("Rol '{}' asignado al usuario {} en Keycloak", roleName, keycloakId);
        } catch (Exception e) {
            log.error("No se pudo asignar el rol '{}' en Keycloak. Verificar que el rol exista en el realm.", roleName, e);
            throw new IllegalStateException("Error asignando rol '" + roleName + "' en Keycloak. Verificar que el rol exista en el realm.");
        }
    }
}
