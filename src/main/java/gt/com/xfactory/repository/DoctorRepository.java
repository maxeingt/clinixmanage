package gt.com.xfactory.repository;

import gt.com.xfactory.entity.DoctorEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DoctorRepository implements PanacheRepository<DoctorEntity> {
}
