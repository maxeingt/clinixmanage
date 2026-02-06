package gt.com.xfactory.repository;

import gt.com.xfactory.entity.DoctorEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class DoctorRepository implements PanacheRepository<DoctorEntity> {

    public Optional<DoctorEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public Optional<DoctorEntity> findByUserKeycloakId(String keycloakId) {
        return find("user.keycloakId", keycloakId).firstResultOptional();
    }
}
