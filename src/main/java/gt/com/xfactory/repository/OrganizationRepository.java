package gt.com.xfactory.repository;

import gt.com.xfactory.entity.*;
import io.quarkus.hibernate.orm.panache.*;
import jakarta.enterprise.context.*;

import java.util.*;

@ApplicationScoped
public class OrganizationRepository implements PanacheRepository<OrganizationEntity> {

    public Optional<OrganizationEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public Optional<OrganizationEntity> findBySlug(String slug) {
        return find("slug", slug).firstResultOptional();
    }
}
