package gt.com.xfactory.repository;

import gt.com.xfactory.entity.PatientEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PatientRepository implements PanacheRepository<PatientEntity> {

    public Optional<PatientEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }
}
