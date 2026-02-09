package gt.com.xfactory.repository;

import gt.com.xfactory.entity.PrescriptionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PrescriptionRepository implements PanacheRepository<PrescriptionEntity> {

    public List<PrescriptionEntity> findByPatientId(UUID patientId) {
        return find("patient.id", patientId).list();
    }

    public List<PrescriptionEntity> findByPatientIdAndDoctorId(UUID patientId, UUID doctorId) {
        return find("patient.id = ?1 AND doctor.id = ?2", patientId, doctorId).list();
    }

    public List<PrescriptionEntity> findByMedicalRecordId(UUID medicalRecordId) {
        return find("medicalRecord.id", medicalRecordId).list();
    }

    public List<PrescriptionEntity> findByDoctorId(UUID doctorId) {
        return find("doctor.id", doctorId).list();
    }

    public List<PrescriptionEntity> findActiveByPatientId(UUID patientId) {
        return find("patient.id = ?1 AND expiryDate >= ?2", patientId, LocalDate.now()).list();
    }

    public List<PrescriptionEntity> findActiveByPatientIdAndDoctorId(UUID patientId, UUID doctorId) {
        return find("patient.id = ?1 AND doctor.id = ?2 AND expiryDate >= ?3", patientId, doctorId, LocalDate.now()).list();
    }

    public Optional<PrescriptionEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }
}
