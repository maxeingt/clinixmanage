package gt.com.xfactory.repository;

import gt.com.xfactory.entity.DistributorEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class DistributorRepository implements PanacheRepository<DistributorEntity> {

    public Optional<DistributorEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public List<DistributorEntity> findAllActive() {
        return find("active", true).list();
    }

    public Optional<DistributorEntity> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
}
