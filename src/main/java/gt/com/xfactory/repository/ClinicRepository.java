package gt.com.xfactory.repository;

import gt.com.xfactory.entity.ClinicEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ClinicRepository implements PanacheRepository<ClinicEntity> {

    public Optional<ClinicEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }
}
