package gt.com.xfactory.repository;

import gt.com.xfactory.entity.MedicalHistGynecoObstetricEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class MedicalHistGynecoObstetricRepository implements PanacheRepository<MedicalHistGynecoObstetricEntity> {

    public List<MedicalHistGynecoObstetricEntity> findByPatientId(UUID patientId) {
        return find("patient.id", patientId).list();
    }

    public Optional<MedicalHistGynecoObstetricEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }
}
