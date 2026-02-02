package gt.com.xfactory.repository;

import gt.com.xfactory.entity.DiagnosisCatalogEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

@ApplicationScoped
public class DiagnosisCatalogRepository implements PanacheRepository<DiagnosisCatalogEntity> {

    public Optional<DiagnosisCatalogEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }
}
