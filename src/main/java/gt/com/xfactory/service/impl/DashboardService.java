package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.entity.enums.*;
import gt.com.xfactory.repository.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;

import java.time.*;
import java.time.temporal.*;
import java.util.*;

@ApplicationScoped
@Slf4j
public class DashboardService {

    @Inject
    MedicalAppointmentRepository medicalAppointmentRepository;

    @Inject
    SecurityContextService securityContextService;

    private UUID getCurrentDoctorId() {
        return securityContextService.getCurrentDoctorId();
    }

    public DashboardDto getDashboardMetrics(UUID clinicId, UUID doctorId) {
        UUID currentDoctorId = getCurrentDoctorId();
        if (currentDoctorId != null) {
            doctorId = currentDoctorId;
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        LocalDateTime startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1).atStartOfDay();

        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        String baseWhere = buildBaseWhere(clinicId, doctorId);
        Map<String, Object> baseParams = buildBaseParams(clinicId, doctorId);

        // Params for main query (today + monthly)
        Map<String, Object> mainParams = new HashMap<>(baseParams);
        mainParams.put("startOfDay", startOfDay);
        mainParams.put("endOfDay", endOfDay);
        mainParams.put("startOfMonth", startOfMonth);
        mainParams.put("endOfMonth", endOfMonth);
        mainParams.put("completed", AppointmentStatus.completed);
        mainParams.put("cancelled", AppointmentStatus.cancelled);
        mainParams.put("noShow", AppointmentStatus.no_show);
        mainParams.put("pendingStatuses", List.of(
                AppointmentStatus.scheduled, AppointmentStatus.confirmed, AppointmentStatus.reopened));

        // 1 query: today metrics (total, completed, pending, cancelled, no_show) + monthly metrics (total, cancellations)
        String jpql = "SELECT "
                + "SUM(CASE WHEN m.appointmentDate >= :startOfDay AND m.appointmentDate < :endOfDay THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN m.appointmentDate >= :startOfDay AND m.appointmentDate < :endOfDay AND m.status = :completed THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN m.appointmentDate >= :startOfDay AND m.appointmentDate < :endOfDay AND m.status IN :pendingStatuses THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN m.appointmentDate >= :startOfDay AND m.appointmentDate < :endOfDay AND m.status = :cancelled THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN m.appointmentDate >= :startOfDay AND m.appointmentDate < :endOfDay AND m.status = :noShow THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN m.appointmentDate >= :startOfMonth AND m.appointmentDate < :endOfMonth THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN m.appointmentDate >= :startOfMonth AND m.appointmentDate < :endOfMonth AND m.status = :cancelled THEN 1 ELSE 0 END) "
                + "FROM MedicalAppointmentEntity m WHERE " + baseWhere;

        var mainQuery = medicalAppointmentRepository.getEntityManager().createQuery(jpql, Object[].class);
        mainParams.forEach(mainQuery::setParameter);
        Object[] row = mainQuery.getSingleResult();

        // 1 query: weekly distinct patients
        Map<String, Object> weeklyParams = new HashMap<>(baseParams);
        weeklyParams.put("startOfWeek", startOfWeek);
        weeklyParams.put("endOfWeek", endOfWeek);
        weeklyParams.put("completed", AppointmentStatus.completed);

        String weeklyJpql = "SELECT COUNT(DISTINCT m.patient.id) FROM MedicalAppointmentEntity m WHERE "
                + baseWhere
                + " AND m.appointmentDate >= :startOfWeek AND m.appointmentDate < :endOfWeek AND m.status = :completed";
        var weeklyQuery = medicalAppointmentRepository.getEntityManager().createQuery(weeklyJpql, Long.class);
        weeklyParams.forEach(weeklyQuery::setParameter);
        long weeklyPatientsAttended = weeklyQuery.getSingleResult();

        return DashboardDto.builder()
                .todayAppointments(toLong(row[0]))
                .todayCompleted(toLong(row[1]))
                .todayPending(toLong(row[2]))
                .todayCancelled(toLong(row[3]))
                .todayNoShow(toLong(row[4]))
                .weeklyPatientsAttended(weeklyPatientsAttended)
                .monthlyAppointments(toLong(row[5]))
                .monthlyCancellations(toLong(row[6]))
                .build();
    }

    private String buildBaseWhere(UUID clinicId, UUID doctorId) {
        StringBuilder sb = new StringBuilder("1 = 1");
        if (clinicId != null) {
            sb.append(" AND m.clinic.id = :clinicId");
        }
        if (doctorId != null) {
            sb.append(" AND m.doctor.id = :doctorId");
        }
        return sb.toString();
    }

    private Map<String, Object> buildBaseParams(UUID clinicId, UUID doctorId) {
        Map<String, Object> params = new HashMap<>();
        if (clinicId != null) {
            params.put("clinicId", clinicId);
        }
        if (doctorId != null) {
            params.put("doctorId", doctorId);
        }
        return params;
    }

    private long toLong(Object value) {
        return value != null ? ((Number) value).longValue() : 0L;
    }
}
