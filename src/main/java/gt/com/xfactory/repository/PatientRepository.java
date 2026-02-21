package gt.com.xfactory.repository;

import gt.com.xfactory.entity.PatientEntity;
import gt.com.xfactory.utils.FilterBuilder;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.*;
import java.util.*;

@ApplicationScoped
public class PatientRepository implements PanacheRepository<PatientEntity> {

    public Optional<PatientEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public Optional<PatientEntity> findByDpi(String dpi) {
        return find("dpi", dpi).firstResultOptional();
    }

    public List<PatientEntity> findByNameAndBirthdate(String firstName, String lastName, LocalDate birthdate) {
        return find("LOWER(firstName) = LOWER(?1) AND LOWER(lastName) = LOWER(?2) AND birthdate = ?3",
                firstName, lastName, birthdate).list();
    }

    public List<PatientEntity> searchByTerm(String q) {
        Map<String, Object> params = new HashMap<>();
        String nameCondition = FilterBuilder.buildNameTokenCondition(q, "firstName", "lastName", params, "n");
        params.put("dpi", q);
        params.put("phone", q);
        return find(nameCondition +
                " OR dpi LIKE CONCAT('%', :dpi, '%')" +
                " OR phone LIKE CONCAT('%', :phone, '%')",
                params)
                .page(0, 20)
                .list();
    }
}
