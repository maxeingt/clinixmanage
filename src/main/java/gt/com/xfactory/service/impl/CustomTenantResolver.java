package gt.com.xfactory.service.impl;

import gt.com.xfactory.utils.*;
import io.quarkus.hibernate.orm.*;
import io.quarkus.hibernate.orm.runtime.tenant.*;
import io.vertx.core.json.*;
import io.vertx.ext.web.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import lombok.extern.slf4j.*;

import java.util.*;

/**
 * Resuelve el tenant ID para Hibernate DISCRIMINATOR multi-tenancy.
 * Usa múltiples estrategias para extraer organization_id del JWT.
 */
@PersistenceUnitExtension
@ApplicationScoped
@Slf4j
public class CustomTenantResolver implements TenantResolver {

    private static final String DEFAULT_TENANT = "00000000-0000-0000-0000-000000000001";

    @Inject
    TenantContext tenantContext;

    @Inject
    RoutingContext routingContext;

    @Override
    public String getDefaultTenantId() {
        return DEFAULT_TENANT;
    }

    @Override
    public String resolveTenantId() {
        // Estrategia 1: TenantContext override (super_admin cross-tenant)
        try {
            String override = tenantContext.get();
            if (override != null) {
                return override;
            }
        } catch (ContextNotActiveException e) {
            // Fuera de request scope
        }

        // Estrategia 2: RoutingContext principal (Quarkus OIDC decoded token)
        try {
            if (routingContext != null && routingContext.user() != null) {
                JsonObject principal = routingContext.user().principal();
                if (principal != null && principal.containsKey("organization_id")) {
                    String orgId = principal.getString("organization_id");
                    if (orgId != null && !orgId.isBlank()) {
                        return orgId;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[TenantResolver] Error en RoutingContext: {}", e.getMessage());
        }

        // Estrategia 3: Decodificar JWT manualmente del header Authorization
        try {
            if (routingContext != null) {
                String authHeader = routingContext.request().getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    String[] parts = token.split("\\.");
                    if (parts.length >= 2) {
                        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                        JsonObject claims = new JsonObject(payload);
                        String orgId = claims.getString("organization_id");
                        if (orgId != null && !orgId.isBlank()) {
                            return orgId;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[TenantResolver] Error en JWT manual decode: {}", e.getMessage());
        }

        log.warn("[TenantResolver] FALLBACK a DEFAULT_TENANT: {}", DEFAULT_TENANT);
        return DEFAULT_TENANT;
    }
}
