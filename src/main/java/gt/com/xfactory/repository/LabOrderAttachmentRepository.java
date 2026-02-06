package gt.com.xfactory.repository;

import gt.com.xfactory.entity.LabOrderAttachmentEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

@ApplicationScoped
public class LabOrderAttachmentRepository implements PanacheRepository<LabOrderAttachmentEntity> {

    public List<LabOrderAttachmentEntity> findByLabOrderId(UUID labOrderId) {
        return find("labOrder.id", labOrderId).list();
    }

    public Optional<LabOrderAttachmentEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }
}
