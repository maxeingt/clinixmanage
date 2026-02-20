package gt.com.xfactory.service.impl;

import gt.com.xfactory.entity.*;
import gt.com.xfactory.repository.*;
import io.quarkus.security.identity.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;
import org.eclipse.microprofile.jwt.*;

import java.util.*;

@ApplicationScoped
@Slf4j
public class SecurityContextService {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @Inject
    UserRepository userRepository;

    @Inject
    DoctorRepository doctorRepository;

    /**
     * Retorna el UUID del doctor actual si el usuario tiene rol doctor.
     * Retorna null si es admin o secretary (ven todos los recursos).
     */
    @SuppressWarnings("unchecked")
    public UUID getCurrentDoctorId() {
        if (securityIdentity.hasRole("admin") || securityIdentity.hasRole("secretary")) {
            return null;
        }
        String keycloakId = jwt.getSubject();
        // Native query para bypasear @TenantId en la resolución del doctor actual
        List<Object[]> results = doctorRepository.getEntityManager()
                .createNativeQuery("SELECT d.id FROM doctor d JOIN \"user\" u ON d.user_id = u.id WHERE u.keycloak_id = :keycloakId")
                .setParameter("keycloakId", keycloakId)
                .getResultList();

        if (results.isEmpty()) {
            throw new ForbiddenException("Doctor no encontrado para el usuario actual");
        }
        Object id = results.get(0);
        return id instanceof UUID ? (UUID) id : UUID.fromString(id.toString());
    }

    public boolean hasRole(String role) {
        return securityIdentity.hasRole(role);
    }

    public String getUserName() {
        return jwt.getName();
    }

    public String getSubject() {
        return jwt.getSubject();
    }

    @SuppressWarnings("unchecked")
    public UUID getCurrentUserId() {
        String keycloakId = jwt.getSubject();
        // Native query para bypasear @TenantId: el tenant podría no estar resuelto
        // correctamente cuando se consulta el usuario del token actual.
        List<Object[]> results = userRepository.getEntityManager()
                .createNativeQuery("SELECT id FROM \"user\" WHERE keycloak_id = :keycloakId")
                .setParameter("keycloakId", keycloakId)
                .getResultList();

        if (results.isEmpty()) {
            throw new ForbiddenException("Usuario no encontrado");
        }
        Object id = results.get(0);
        return id instanceof UUID ? (UUID) id : UUID.fromString(id.toString());
    }

    public void validateOwnAccess(UUID requestedUserId) {
        if (securityIdentity.hasRole("admin")) return;
        UUID currentUserId = getCurrentUserId();
        if (!requestedUserId.equals(currentUserId)) {
            throw new ForbiddenException("No tiene acceso a este recurso");
        }
    }

    public UUID getCurrentOrganizationId() {
        Object orgClaim = jwt.getClaim("organization_id");
        if (orgClaim == null) {
            throw new ForbiddenException("Token no contiene organization_id");
        }
        return UUID.fromString(orgClaim.toString());
    }

    public String getCurrentOrganizationSlug() {
        Object slugClaim = jwt.getClaim("organization_slug");
        if (slugClaim == null) {
            return null;
        }
        return slugClaim.toString();
    }

    public void validateDoctorOwnership(UUID resourceDoctorId) {
        UUID currentDoctorId = getCurrentDoctorId();
        if (currentDoctorId != null && !currentDoctorId.equals(resourceDoctorId)) {
            throw new ForbiddenException("No tiene acceso a este recurso");
        }
    }

    public void validateOwnDoctorAccess(UUID requestedDoctorId) {
        if (securityIdentity.hasRole("admin")) return;
        UUID currentDoctorId = getCurrentDoctorId();
        if (!requestedDoctorId.equals(currentDoctorId)) {
            throw new ForbiddenException("No tiene acceso a las notificaciones de otro doctor");
        }
    }
}
