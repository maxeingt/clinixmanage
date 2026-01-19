package gt.com.xfactory.repository;

import gt.com.xfactory.entity.SpecialtyFormTemplateEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SpecialtyFormTemplateRepository implements PanacheRepository<SpecialtyFormTemplateEntity> {

    public List<SpecialtyFormTemplateEntity> findBySpecialtyId(UUID specialtyId) {
        return find("specialty.id", specialtyId).list();
    }

    public List<SpecialtyFormTemplateEntity> findActiveBySpecialtyId(UUID specialtyId) {
        return find("specialty.id = ?1 AND isActive = true", specialtyId).list();
    }

    public Optional<SpecialtyFormTemplateEntity> findLatestBySpecialtyIdAndFormName(UUID specialtyId, String formName) {
        return find("specialty.id = ?1 AND formName = ?2 AND isActive = true ORDER BY version DESC",
                    specialtyId, formName).firstResultOptional();
    }

    public Optional<SpecialtyFormTemplateEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public List<SpecialtyFormTemplateEntity> findAllActive() {
        return find("isActive = true").list();
    }
}
