package gt.com.xfactory.repository;

import gt.com.xfactory.entity.SpecialtyEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SpecialtyRepository implements PanacheRepository<SpecialtyEntity> {

    public Optional<SpecialtyEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }
}
