package gt.com.xfactory.repository;

import gt.com.xfactory.entity.RoleTemplateEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RoleTemplateRepository implements PanacheRepository<RoleTemplateEntity> {

    public Optional<RoleTemplateEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public Optional<RoleTemplateEntity> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
}
