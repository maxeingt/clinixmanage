package gt.com.xfactory.repository;

import gt.com.xfactory.entity.PatientEntity;
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
        return find(
                "LOWER(firstName) LIKE LOWER(CONCAT('%', :q, '%')) " +
                "OR LOWER(lastName) LIKE LOWER(CONCAT('%', :q, '%')) " +
                "OR dpi LIKE CONCAT('%', :q, '%') " +
                "OR phone LIKE CONCAT('%', :q, '%')",
                Map.of("q", q))
                .page(0, 20)
                .list();
    }
}
