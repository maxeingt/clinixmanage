package gt.com.xfactory.repository;

import gt.com.xfactory.entity.PrescriptionMedicationEntity;
import gt.com.xfactory.entity.PrescriptionMedicationId;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PrescriptionMedicationRepository implements PanacheRepositoryBase<PrescriptionMedicationEntity, PrescriptionMedicationId> {

    public List<PrescriptionMedicationEntity> findByPrescriptionId(UUID prescriptionId) {
        return find("prescription.id", prescriptionId).list();
    }

    public List<PrescriptionMedicationEntity> findByMedicationId(UUID medicationId) {
        return find("medication.id", medicationId).list();
    }

    public long deleteByPrescriptionId(UUID prescriptionId) {
        return delete("prescription.id", prescriptionId);
    }
}
