package gt.com.xfactory.repository;

import gt.com.xfactory.entity.PharmaceuticalEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PharmaceuticalRepository implements PanacheRepository<PharmaceuticalEntity> {

    public Optional<PharmaceuticalEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public List<PharmaceuticalEntity> findAllActive() {
        return find("active", true).list();
    }

    public Optional<PharmaceuticalEntity> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
}
