package gt.com.xfactory.repository;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.request.filter.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.utils.*;
import io.quarkus.hibernate.orm.panache.*;
import jakarta.enterprise.context.*;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.transaction.*;

import java.util.*;
import java.util.stream.*;

@ApplicationScoped
public class DoctorClinicRepository implements PanacheRepository<DoctorClinicEntity> {

    @PersistenceContext
    EntityManager em;

    @Transactional
    public PageResponse<DoctorEntity> findDoctorsByClinic(UUID clinicId, DoctorFilterDto filter, CommonPageRequest pageRequest) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

// Total count
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<DoctorClinicEntity> rootCount = countQuery.from(DoctorClinicEntity.class);
        Join<DoctorClinicEntity, DoctorEntity> doctorJoinCount = rootCount.join("doctor");

        Predicate predicateCount = createBasePredicate(cb, rootCount, doctorJoinCount, clinicId, filter);
        countQuery.select(cb.countDistinct(doctorJoinCount)).where(predicateCount);

        Long total = em.createQuery(countQuery).getSingleResult();

        if (total == 0L) {
            return new PageResponse<>(Collections.emptyList(), 0, pageRequest.getPage(), pageRequest.getSize());
        }

        CriteriaQuery<DoctorEntity> cq = cb.createQuery(DoctorEntity.class);
        Root<DoctorClinicEntity> root = cq.from(DoctorClinicEntity.class);
        Join<DoctorClinicEntity, DoctorEntity> doctorJoin = root.join("doctor");

        Predicate predicate = createBasePredicate(cb, root, doctorJoin, clinicId, filter);

        cq.select(doctorJoin).where(predicate).distinct(true);

// Ordenamiento
        List<Order> orders = SortUtils.toCriteriaOrders(cb, doctorJoin, pageRequest.getSort(), "id");
        cq.orderBy(orders);

        TypedQuery<DoctorEntity> query = em.createQuery(cq);
        query.setFirstResult(pageRequest.getPage() * pageRequest.getSize());
        query.setMaxResults(pageRequest.getSize());

        List<DoctorEntity> results = query.getResultList();

        return new PageResponse<>(results, total.intValue(), pageRequest.getPage(), pageRequest.getSize());

    }

    private Predicate createBasePredicate(CriteriaBuilder cb, Root<DoctorClinicEntity> root,
                                        Join<DoctorClinicEntity, DoctorEntity> doctorJoin,
                                        UUID clinicId, DoctorFilterDto filter) {
        Predicate predicate = cb.equal(root.get("clinic").get("id"), clinicId);

        if (Objects.nonNull(filter)) {
            predicate = QueryUtils.addLikePredicate(cb, doctorJoin, predicate, "firstName", filter.firstName);
            predicate = QueryUtils.addLikePredicate(cb, doctorJoin, predicate, "lastName", filter.lastName);
            predicate = QueryUtils.addLikePredicate(cb, doctorJoin, predicate, "email", filter.mail);
            predicate = QueryUtils.addEqualPredicate(cb, root, predicate, "active", true);
        }

        return predicate;
    }

    public java.util.Optional<DoctorClinicEntity> findByDoctorIdAndClinicId(UUID doctorId, UUID clinicId) {
        return find("id.doctorId = ?1 and id.clinicId = ?2", doctorId, clinicId).firstResultOptional();
    }

    public List<DoctorClinicEntity> findByDoctorId(UUID doctorId) {
        return list("id.doctorId", doctorId);
    }

    public Map<UUID, List<DoctorClinicEntity>> findByDoctorIds(List<UUID> doctorIds) {
        if (doctorIds == null || doctorIds.isEmpty()) return Collections.emptyMap();
        return list("id.doctorId in ?1", doctorIds)
                .stream()
                .collect(Collectors.groupingBy(dc -> dc.getId().getDoctorId()));
    }

    public long deleteByDoctorId(UUID doctorId) {
        return delete("id.doctorId", doctorId);
    }
}
