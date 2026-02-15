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
    public UUID getCurrentDoctorId() {
        if (securityIdentity.hasRole("admin") || securityIdentity.hasRole("secretary")) {
            return null;
        }
        String keycloakId = jwt.getSubject();
        return doctorRepository.findByUserKeycloakId(keycloakId)
                .map(DoctorEntity::getId)
                .orElseThrow(() -> new ForbiddenException("Doctor no encontrado para el usuario actual"));
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

    public UUID getCurrentUserId() {
        String keycloakId = jwt.getSubject();
        return userRepository.findByKeycloakId(keycloakId)
                .map(UserEntity::getId)
                .orElseThrow(() -> new ForbiddenException("Usuario no encontrado"));
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

    public void validateOwnDoctorAccess(UUID requestedDoctorId) {
        if (securityIdentity.hasRole("admin")) return;
        String keycloakId = jwt.getSubject();
        UUID currentDoctorId = doctorRepository.findByUserKeycloakId(keycloakId)
                .map(DoctorEntity::getId)
                .orElseThrow(() -> new ForbiddenException("Doctor no encontrado para el usuario actual"));
        if (!requestedDoctorId.equals(currentDoctorId)) {
            throw new ForbiddenException("No tiene acceso a las notificaciones de otro doctor");
        }
    }
}
