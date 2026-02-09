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
}
