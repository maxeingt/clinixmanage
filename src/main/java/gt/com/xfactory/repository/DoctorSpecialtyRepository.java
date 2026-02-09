package gt.com.xfactory.repository;

import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import io.quarkus.hibernate.orm.panache.*;
import jakarta.enterprise.context.*;

import java.util.*;
import java.util.stream.*;

@ApplicationScoped
public class DoctorSpecialtyRepository implements PanacheRepository<DoctorSpecialtyEntity> {

    public List<SpecialtyDto> findSpecialtiesByDoctorId(UUID doctorId) {
        return find("id.doctorId", doctorId)
                .stream()
                .map(ds -> SpecialtyDto.builder()
                        .id(ds.getSpecialty().getId())
                        .name(ds.getSpecialty().getName())
                        .description(ds.getSpecialty().getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    public List<DoctorEntity> findDoctorsBySpecialtyId(UUID specialtyId) {
        return find("id.specialtyId", specialtyId)
                .stream()
                .map(DoctorSpecialtyEntity::getDoctor)
                .collect(Collectors.toList());
    }

    public java.util.Optional<DoctorSpecialtyEntity> findByDoctorIdAndSpecialtyId(UUID doctorId, UUID specialtyId) {
        return find("id.doctorId = ?1 and id.specialtyId = ?2", doctorId, specialtyId).firstResultOptional();
    }

    public Map<UUID, List<SpecialtyDto>> findSpecialtiesByDoctorIds(List<UUID> doctorIds) {
        if (doctorIds == null || doctorIds.isEmpty()) return Collections.emptyMap();
        return find("id.doctorId in ?1", doctorIds)
                .stream()
                .collect(Collectors.groupingBy(
                        ds -> ds.getId().getDoctorId(),
                        Collectors.mapping(ds -> SpecialtyDto.builder()
                                .id(ds.getSpecialty().getId())
                                .name(ds.getSpecialty().getName())
                                .description(ds.getSpecialty().getDescription())
                                .build(), Collectors.toList())
                ));
    }

    public long deleteByDoctorId(UUID doctorId) {
        return delete("id.doctorId", doctorId);
    }
}
