package gt.com.xfactory.repository;

import gt.com.xfactory.entity.*;
import io.quarkus.hibernate.orm.panache.*;
import jakarta.enterprise.context.*;

import java.util.*;
import java.util.stream.*;

@ApplicationScoped
public class DoctorSpecialtyRepository implements PanacheRepository<DoctorSpecialtyEntity> {

    public List<DoctorSpecialtyEntity> findByDoctorId(UUID doctorId) {
        return find("id.doctorId", doctorId).list();
    }

    public List<DoctorEntity> findDoctorsBySpecialtyId(UUID specialtyId) {
        return find("id.specialtyId", specialtyId)
                .stream()
                .map(DoctorSpecialtyEntity::getDoctor)
                .collect(Collectors.toList());
    }

    public Optional<DoctorSpecialtyEntity> findByDoctorIdAndSpecialtyId(UUID doctorId, UUID specialtyId) {
        return find("id.doctorId = ?1 and id.specialtyId = ?2", doctorId, specialtyId).firstResultOptional();
    }

    public Map<UUID, List<DoctorSpecialtyEntity>> findByDoctorIds(List<UUID> doctorIds) {
        if (doctorIds == null || doctorIds.isEmpty()) return Collections.emptyMap();
        return find("id.doctorId in ?1", doctorIds)
                .stream()
                .collect(Collectors.groupingBy(ds -> ds.getId().getDoctorId()));
    }

    public long deleteByDoctorId(UUID doctorId) {
        return delete("id.doctorId", doctorId);
    }
}
