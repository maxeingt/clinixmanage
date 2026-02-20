package gt.com.xfactory.repository;

import gt.com.xfactory.entity.MedicalRecordEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class MedicalRecordRepository implements PanacheRepository<MedicalRecordEntity> {

    public List<MedicalRecordEntity> findByPatientId(UUID patientId) {
        return find("patient.id", patientId).list();
    }

    public List<MedicalRecordEntity> findByPatientIdAndDoctorId(UUID patientId, UUID doctorId) {
        return find("patient.id = ?1 AND doctor.id = ?2", patientId, doctorId).list();
    }

    public List<MedicalRecordEntity> findByAppointmentId(UUID appointmentId) {
        return find("appointment.id", appointmentId).list();
    }

    public List<MedicalRecordEntity> findByDoctorId(UUID doctorId) {
        return find("doctor.id", doctorId).list();
    }

    public List<MedicalRecordEntity> findByPatientIdAndSpecialtyId(UUID patientId, UUID specialtyId) {
        return find("patient.id = ?1 AND specialty.id = ?2", patientId, specialtyId).list();
    }

    public Optional<MedicalRecordEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }
}
