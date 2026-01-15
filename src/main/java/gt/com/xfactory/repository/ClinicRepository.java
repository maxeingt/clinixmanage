package gt.com.xfactory.repository;

import gt.com.xfactory.entity.ClinicEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClinicRepository implements PanacheRepository<ClinicEntity> {
}
