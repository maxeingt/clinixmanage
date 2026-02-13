package gt.com.xfactory.repository;

import gt.com.xfactory.entity.*;
import io.quarkus.hibernate.orm.panache.*;
import jakarta.enterprise.context.*;

import java.util.*;

@ApplicationScoped
public class SpecialtyFormTemplateRepository implements PanacheRepository<SpecialtyFormTemplateEntity> {

    public List<SpecialtyFormTemplateEntity> findBySpecialtyId(UUID specialtyId) {
        return find("specialty.id", specialtyId).list();
    }

    public List<SpecialtyFormTemplateEntity> findActiveBySpecialtyId(UUID specialtyId) {
        return find("specialty.id = ?1 AND isActive = true", specialtyId).list();
    }

    public List<SpecialtyFormTemplateEntity> findAllBySpecialtyId(UUID specialtyId) {
        return find("specialty.id = ?1 ORDER BY formName, version DESC", specialtyId).list();
    }

    public Optional<SpecialtyFormTemplateEntity> findLatestBySpecialtyIdAndFormName(UUID specialtyId, String formName) {
        return find("specialty.id = ?1 AND formName = ?2 ORDER BY version DESC",
                    specialtyId, formName).firstResultOptional();
    }

    public Optional<SpecialtyFormTemplateEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public List<SpecialtyFormTemplateEntity> findAllActive() {
        return find("isActive = true").list();
    }

    public long deactivateBySpecialtyAndFormName(UUID specialtyId, String formName) {
        return update("isActive = false WHERE specialty.id = ?1 AND formName = ?2 AND isActive = true",
                specialtyId, formName);
    }

    public Optional<Integer> findMaxVersion(UUID specialtyId, String formName) {
        return find("specialty.id = ?1 AND formName = ?2 ORDER BY version DESC",
                specialtyId, formName)
                .firstResultOptional()
                .map(SpecialtyFormTemplateEntity::getVersion);
    }
}
