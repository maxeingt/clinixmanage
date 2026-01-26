package gt.com.xfactory.repository;

import gt.com.xfactory.entity.MedicationEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class MedicationRepository implements PanacheRepository<MedicationEntity> {

    public Optional<MedicationEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public List<MedicationEntity> findAllActive() {
        return find("active", true).list();
    }

    public Optional<MedicationEntity> findByCode(String code) {
        return find("code", code).firstResultOptional();
    }

    public List<MedicationEntity> findByPharmaceuticalId(UUID pharmaceuticalId) {
        return find("pharmaceutical.id", pharmaceuticalId).list();
    }

    public List<MedicationEntity> findByDistributorId(UUID distributorId) {
        return find("distributor.id", distributorId).list();
    }

    public List<MedicationEntity> findByActiveIngredient(String activeIngredient) {
        return find("lower(activeIngredient) like lower(?1)", "%" + activeIngredient + "%").list();
    }
}
