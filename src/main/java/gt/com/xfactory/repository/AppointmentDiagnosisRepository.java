package gt.com.xfactory.repository;

import gt.com.xfactory.entity.AppointmentDiagnosisEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

@ApplicationScoped
public class AppointmentDiagnosisRepository implements PanacheRepository<AppointmentDiagnosisEntity> {

    public List<AppointmentDiagnosisEntity> findByAppointmentId(UUID appointmentId) {
        return list("appointment.id", appointmentId);
    }

    public long deleteByAppointmentId(UUID appointmentId) {
        return delete("appointment.id", appointmentId);
    }
}
