package gt.com.xfactory.repository;

import gt.com.xfactory.entity.UserClinicPermissionEntity;
import gt.com.xfactory.entity.UserClinicPermissionId;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserClinicPermissionRepository implements PanacheRepositoryBase<UserClinicPermissionEntity, UserClinicPermissionId> {

    public List<UserClinicPermissionEntity> findByUserId(UUID userId) {
        return list("id.userId", userId);
    }

    public List<UserClinicPermissionEntity> findByClinicId(UUID clinicId) {
        return list("id.clinicId", clinicId);
    }

    public Optional<UserClinicPermissionEntity> findByUserIdAndClinicId(UUID userId, UUID clinicId) {
        return find("id.userId = ?1 and id.clinicId = ?2", userId, clinicId).firstResultOptional();
    }

    public List<UUID> findClinicIdsByUserId(UUID userId) {
        return getEntityManager()
                .createQuery("SELECT ucp.id.clinicId FROM UserClinicPermissionEntity ucp WHERE ucp.id.userId = :userId", UUID.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public boolean hasAccessToClinic(UUID userId, UUID clinicId) {
        return findByUserIdAndClinicId(userId, clinicId).isPresent();
    }
}
