package gt.com.xfactory.repository;

import gt.com.xfactory.entity.LabResultEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

@ApplicationScoped
public class LabResultRepository implements PanacheRepository<LabResultEntity> {

    public List<LabResultEntity> findByLabOrderId(UUID labOrderId) {
        return find("labOrder.id", labOrderId).list();
    }

    public Optional<LabResultEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }
}
