package gt.com.xfactory.repository;

import gt.com.xfactory.entity.LabOrderEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

@ApplicationScoped
public class LabOrderRepository implements PanacheRepository<LabOrderEntity> {

    public List<LabOrderEntity> findByPatientId(UUID patientId) {
        return find("patient.id", patientId).list();
    }

    public List<LabOrderEntity> findByPatientIdAndDoctorId(UUID patientId, UUID doctorId) {
        return find("patient.id = ?1 AND doctor.id = ?2", patientId, doctorId).list();
    }

    public List<LabOrderEntity> findByDoctorId(UUID doctorId) {
        return find("doctor.id", doctorId).list();
    }

    public List<LabOrderEntity> findByAppointmentId(UUID appointmentId) {
        return find("appointment.id", appointmentId).list();
    }

    public Optional<LabOrderEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }
}
