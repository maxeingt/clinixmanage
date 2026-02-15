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
            if (filter.specialtyId != null) {
                query.append(" AND specialty.id = :specialtyId");
                params.put("specialtyId", filter.specialtyId);
            }
            if (filter.source != null) {
                query.append(" AND source = :source");
                params.put("source", filter.source);
            }
            if (filter.startDate != null) {
                query.append(" AND appointmentDate >= :startDate");
                params.put("startDate", filter.startDate);
            }
            if (filter.endDate != null) {
                query.append(" AND appointmentDate <= :endDate");
                params.put("endDate", filter.endDate);
            }
            if (filter.status != null) {
                query.append(" AND status = :status");
                params.put("status", filter.status);
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
            if (filter.patientId != null) {
                query.append(" AND patient.id = :patientId");
                params.put("patientId", filter.patientId);
            }
            if (filter.specialtyId != null) {
                query.append(" AND specialty.id = :specialtyId");
                params.put("specialtyId", filter.specialtyId);
            }
            if (filter.source != null) {
                query.append(" AND source = :source");
                params.put("source", filter.source);
            }
            if (filter.startDate != null) {
                query.append(" AND appointmentDate >= :startDate");
                params.put("startDate", filter.startDate);
            }
            if (filter.endDate != null) {
                query.append(" AND appointmentDate <= :endDate");
                params.put("endDate", filter.endDate);
            }
            if (filter.status != null) {
                query.append(" AND status = :status");
                params.put("status", filter.status);
            }
        }

        query.append(" ORDER BY appointmentDate ASC");

        return find(query.toString(), params).list();
    }

    public Optional<UUID> findChildFollowUpId(UUID parentId) {
        return getEntityManager()
                .createQuery("SELECT a.id FROM MedicalAppointmentEntity a WHERE a.followUpAppointment.id = :parentId", UUID.class)
                .setParameter("parentId", parentId)
                .getResultStream()
                .findFirst();
    }
}
