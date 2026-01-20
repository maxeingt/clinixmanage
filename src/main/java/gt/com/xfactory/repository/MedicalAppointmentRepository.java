package gt.com.xfactory.repository;

import gt.com.xfactory.dto.request.filter.MedicalAppointmentFilterDto;
import gt.com.xfactory.entity.MedicalAppointmentEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class MedicalAppointmentRepository implements PanacheRepository<MedicalAppointmentEntity> {

    public List<MedicalAppointmentEntity> findByPatientId(UUID patientId) {
        return find("patient.id", patientId).list();
    }

    public List<MedicalAppointmentEntity> findByPatientIdWithFilters(UUID patientId, MedicalAppointmentFilterDto filter) {
        StringBuilder query = new StringBuilder("patient.id = :patientId");
        Map<String, Object> params = new HashMap<>();
        params.put("patientId", patientId);

        if (filter != null) {
            if (filter.doctorId != null) {
                query.append(" AND doctor.id = :doctorId");
                params.put("doctorId", filter.doctorId);
            }
            if (filter.clinicId != null) {
                query.append(" AND clinic.id = :clinicId");
                params.put("clinicId", filter.clinicId);
            }
        }

        return find(query.toString(), params).list();
    }

    public Optional<MedicalAppointmentEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public List<MedicalAppointmentEntity> findByClinicId(UUID clinicId, MedicalAppointmentFilterDto filter) {
        StringBuilder query = new StringBuilder("clinic.id = :clinicId");
        Map<String, Object> params = new HashMap<>();
        params.put("clinicId", clinicId);

        if (filter != null) {
            if (filter.doctorId != null) {
                query.append(" AND doctor.id = :doctorId");
                params.put("doctorId", filter.doctorId);
            }
            if (filter.startDate != null) {
                query.append(" AND appointmentDate >= :startDate");
                params.put("startDate", filter.startDate);
            }
            if (filter.endDate != null) {
                query.append(" AND appointmentDate <= :endDate");
                params.put("endDate", filter.endDate);
            }
        }

        query.append(" ORDER BY appointmentDate ASC");

        return find(query.toString(), params).list();
    }
}
