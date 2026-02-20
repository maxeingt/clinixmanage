package gt.com.xfactory.utils;

import jakarta.inject.*;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.*;
import org.eclipse.microprofile.jwt.*;

/**
 * Filtro JAX-RS que se ejecuta después de la autenticación OIDC pero antes de cualquier
 * acceso a base de datos. Extrae organization_id del JWT y lo setea en TenantContext
 * para que CustomTenantResolver lo use como prioridad 1.
 */
@Provider
@Slf4j
public class TenantRequestFilter implements ContainerRequestFilter {

    @Inject
    JsonWebToken jwt;

    @Inject
    TenantContext tenantContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (jwt == null || jwt.getSubject() == null) {
            return;
        }

        // No sobreescribir si ya fue seteado manualmente (ej: super_admin cross-tenant)
        if (tenantContext.get() != null) {
            return;
        }

        Object orgClaim = jwt.getClaim("organization_id");
        if (orgClaim != null) {
            String orgId = orgClaim.toString();
            if (!orgId.isBlank()) {
                tenantContext.set(orgId);
            }
        } else {
            log.warn("TenantRequestFilter: organization_id no presente en JWT para user: {}", jwt.getSubject());
        }
    }
}
