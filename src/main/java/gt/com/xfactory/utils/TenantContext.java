package gt.com.xfactory.utils;

import jakarta.enterprise.context.*;

@RequestScoped
public class TenantContext {

    private String overrideTenantId;

    public void set(String tenantId) {
        this.overrideTenantId = tenantId;
    }

    public String get() {
        return overrideTenantId;
    }

    public void clear() {
        this.overrideTenantId = null;
    }
}
