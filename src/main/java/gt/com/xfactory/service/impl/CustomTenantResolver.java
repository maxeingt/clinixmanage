package gt.com.xfactory.service.impl;

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

    @Override
    public String getDefaultTenantId() {
        return DEFAULT_TENANT;
    }

    @Override
    public String resolveTenantId() {
        try {
            if (routingContext == null || routingContext.user() == null) {
                return DEFAULT_TENANT;
            }

            var token = routingContext.user().principal();
            if (token != null && token.containsKey("organization_id")) {
                String orgId = token.getString("organization_id");
                if (orgId != null && !orgId.isBlank()) {
                    return orgId;
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo resolver tenant ID del JWT, usando default: {}", e.getMessage());
        }
        return DEFAULT_TENANT;
    }
}
