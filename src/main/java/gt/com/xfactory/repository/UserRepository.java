package gt.com.xfactory.repository;

import gt.com.xfactory.entity.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepository<UserEntity> {

    public Optional<UserEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public Optional<UserEntity> findByKeycloakId(String keycloakId) {
        return find("keycloakId", keycloakId).firstResultOptional();
    }

    public Optional<UserEntity> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public Optional<UserEntity> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }
}
