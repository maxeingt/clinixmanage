package gt.com.xfactory.repository;

import gt.com.xfactory.entity.MedicalHistoryPathologicalFamEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MedicalHistoryPathologicalFamRepository implements PanacheRepository<MedicalHistoryPathologicalFamEntity> {

    public List<MedicalHistoryPathologicalFamEntity> findByPatientId(UUID patientId) {
        return find("patient.id", patientId).list();
    }
}
