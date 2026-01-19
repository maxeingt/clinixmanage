package gt.com.xfactory.repository;

import gt.com.xfactory.dto.response.SpecialtyDto;
import gt.com.xfactory.entity.DoctorSpecialtyEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
}
