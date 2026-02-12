package gt.com.xfactory.service.widget;

import gt.com.xfactory.dto.response.DashboardWidgetsDto.*;
import gt.com.xfactory.entity.enums.*;
import gt.com.xfactory.repository.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import lombok.extern.slf4j.*;

import java.time.*;
import java.time.temporal.*;
import java.util.*;

@ApplicationScoped
@Slf4j
public class WeeklySummaryResolver implements WidgetResolver {

    @Inject
    MedicalAppointmentRepository appointmentRepository;

    @Override
    public WidgetType getType() {
        return WidgetType.WEEKLY_SUMMARY;
    }

    @Override
    public Object resolve(UUID clinicId, UUID doctorId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1).atStartOfDay();

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
        params.put("startOfWeek", startOfWeek);
        params.put("endOfWeek", endOfWeek);
        params.put("completed", AppointmentStatus.completed);
        params.put("cancelled", AppointmentStatus.cancelled);
        params.put("noShow", AppointmentStatus.no_show);

        String jpql = "SELECT "
                + "COUNT(m), "
                + "SUM(CASE WHEN m.status = :completed THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN m.status = :cancelled THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN m.status = :noShow THEN 1 ELSE 0 END), "
                + "COUNT(DISTINCT m.patient.id) "
                + "FROM MedicalAppointmentEntity m WHERE " + where
                + " AND m.appointmentDate >= :startOfWeek AND m.appointmentDate < :endOfWeek";

        var query = appointmentRepository.getEntityManager().createQuery(jpql, Object[].class);
        params.forEach(query::setParameter);
        Object[] row = query.getSingleResult();

        return WeeklySummaryData.builder()
                .totalAppointments(toLong(row[0]))
                .completed(toLong(row[1]))
                .cancelled(toLong(row[2]))
                .noShow(toLong(row[3]))
                .patientsAttended(toLong(row[4]))
                .build();
    }

    private long toLong(Object value) {
        return value != null ? ((Number) value).longValue() : 0L;
    }
}
