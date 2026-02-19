package gt.com.xfactory.service.impl;

import gt.com.xfactory.utils.*;
import io.quarkus.hibernate.orm.runtime.tenant.*;
import io.vertx.ext.web.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import lombok.extern.slf4j.*;

@ApplicationScoped
@Slf4j
public class CustomTenantResolver implements TenantResolver {

    private static final String DEFAULT_TENANT = "00000000-0000-0000-0000-000000000001";

    @Inject
    RoutingContext routingContext;

    @Inject
    TenantContext tenantContext;

    @Override
    public String getDefaultTenantId() {
        return DEFAULT_TENANT;
    }

    @Override
    public String resolveTenantId() {
        // Prioridad 1: Override de request scope (para operaciones cross-tenant del super_admin)
        try {
            String override = tenantContext.get();
            if (override != null) {
                return override;
            }
        } catch (ContextNotActiveException e) {
            // Fuera de request scope (startup, health checks, etc.)
        }

        // Prioridad 2: JWT claim
        try {
            if (routingContext != null && routingContext.user() != null) {
                var token = routingContext.user().principal();
                if (token != null && token.containsKey("organization_id")) {
                    String orgId = token.getString("organization_id");
                    if (orgId != null && !orgId.isBlank()) {
                        return orgId;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo resolver tenant ID del JWT, usando default: {}", e.getMessage());
        }

        return DEFAULT_TENANT;
    }
}
