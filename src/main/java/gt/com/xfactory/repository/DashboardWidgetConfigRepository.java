package gt.com.xfactory.repository;

import gt.com.xfactory.entity.*;
import io.quarkus.hibernate.orm.panache.*;
import jakarta.enterprise.context.*;

import java.util.*;

@ApplicationScoped
public class DashboardWidgetConfigRepository implements PanacheRepository<DashboardWidgetConfigEntity> {

    public Optional<DashboardWidgetConfigEntity> findByUserAndClinic(UUID userId, UUID clinicId) {
        return find("id.userId = ?1 AND id.clinicId = ?2", userId, clinicId).firstResultOptional();
    }
}
