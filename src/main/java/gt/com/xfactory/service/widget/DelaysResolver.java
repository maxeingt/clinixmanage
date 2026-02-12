package gt.com.xfactory.service.widget;

import gt.com.xfactory.dto.response.DashboardWidgetsDto.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.entity.enums.*;
import gt.com.xfactory.repository.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import lombok.extern.slf4j.*;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

@ApplicationScoped
@Slf4j
public class DelaysResolver implements WidgetResolver {

    @Inject
    MedicalAppointmentRepository appointmentRepository;

    @Override
    public WidgetType getType() {
        return WidgetType.DELAYS;
    }

    @Override
    public Object resolve(UUID clinicId, UUID doctorId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        StringBuilder where = new StringBuilder("1 = 1");
        Map<String, Object> params = new HashMap<>();

        if (clinicId != null) {
            where.append(" AND m.clinic.id = :clinicId");
            params.put("clinicId", clinicId);
        }
        if (doctorId != null) {
            where.append(" AND m.doctor.id = :doctorId");
            params.put("doctorId", doctorId);
        }
        params.put("now", now);
        params.put("startOfDay", startOfDay);
        params.put("statuses", List.of(AppointmentStatus.scheduled, AppointmentStatus.confirmed, AppointmentStatus.reopened));

        String jpql = "SELECT m FROM MedicalAppointmentEntity m "
                + "LEFT JOIN FETCH m.patient "
                + "WHERE " + where
                + " AND m.appointmentDate >= :startOfDay AND m.appointmentDate < :now "
                + "AND m.status IN :statuses "
                + "ORDER BY m.appointmentDate ASC";

        var query = appointmentRepository.getEntityManager().createQuery(jpql, MedicalAppointmentEntity.class);
        params.forEach(query::setParameter);
        query.setMaxResults(5);

        List<DelayItem> items = query.getResultList().stream()
                .map(apt -> DelayItem.builder()
                        .appointmentId(apt.getId())
                        .patientName(apt.getPatient().getFirstName() + " " + apt.getPatient().getLastName())
                        .scheduledTime(apt.getAppointmentDate().format(DateTimeFormatter.ofPattern("HH:mm")))
                        .delayMinutes(Duration.between(apt.getAppointmentDate(), now).toMinutes())
                        .build())
                .collect(Collectors.toList());

        return DelaysData.builder()
                .count(items.size())
                .items(items)
                .build();
    }
}
