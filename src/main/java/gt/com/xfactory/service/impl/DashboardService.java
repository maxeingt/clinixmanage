package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.response.DashboardDto;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.entity.enums.AppointmentStatus;
import gt.com.xfactory.repository.*;
import io.quarkus.security.identity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
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

        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        List<AppointmentStatus> pendingStatuses = List.of(
                AppointmentStatus.scheduled, AppointmentStatus.confirmed, AppointmentStatus.reopened
        );

        String baseFilter = buildBaseFilter(clinicId, doctorId);
        Map<String, Object> baseParams = buildBaseParams(clinicId, doctorId);

        long todayAppointments = countAppointments(baseFilter
                        + " and appointmentDate >= :startOfDay and appointmentDate < :endOfDay",
                merge(baseParams, Map.of("startOfDay", startOfDay, "endOfDay", endOfDay)));

        long todayCompleted = countAppointments(baseFilter
                        + " and appointmentDate >= :startOfDay and appointmentDate < :endOfDay and status = :status",
                merge(baseParams, Map.of("startOfDay", startOfDay, "endOfDay", endOfDay, "status", AppointmentStatus.completed)));

        long todayPending = countAppointments(baseFilter
                        + " and appointmentDate >= :startOfDay and appointmentDate < :endOfDay and status in :statuses",
                merge(baseParams, Map.of("startOfDay", startOfDay, "endOfDay", endOfDay, "statuses", pendingStatuses)));

        long todayCancelled = countAppointments(baseFilter
                        + " and appointmentDate >= :startOfDay and appointmentDate < :endOfDay and status = :status",
                merge(baseParams, Map.of("startOfDay", startOfDay, "endOfDay", endOfDay, "status", AppointmentStatus.cancelled)));

        long todayNoShow = countAppointments(baseFilter
                        + " and appointmentDate >= :startOfDay and appointmentDate < :endOfDay and status = :status",
                merge(baseParams, Map.of("startOfDay", startOfDay, "endOfDay", endOfDay, "status", AppointmentStatus.no_show)));

        LocalDateTime startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1).atStartOfDay();

        long weeklyPatientsAttended = countDistinctPatients(baseFilter,
                merge(baseParams, Map.of("startOfWeek", startOfWeek, "endOfWeek", endOfWeek, "status", AppointmentStatus.completed)));

        long monthlyAppointments = countAppointments(baseFilter
                        + " and appointmentDate >= :startOfMonth and appointmentDate < :endOfMonth",
                merge(baseParams, Map.of("startOfMonth", startOfMonth, "endOfMonth", endOfMonth)));

        long monthlyCancellations = countAppointments(baseFilter
                        + " and appointmentDate >= :startOfMonth and appointmentDate < :endOfMonth and status = :status",
                merge(baseParams, Map.of("startOfMonth", startOfMonth, "endOfMonth", endOfMonth, "status", AppointmentStatus.cancelled)));

        return DashboardDto.builder()
                .todayAppointments(todayAppointments)
                .todayCompleted(todayCompleted)
                .todayPending(todayPending)
                .todayCancelled(todayCancelled)
                .todayNoShow(todayNoShow)
                .weeklyPatientsAttended(weeklyPatientsAttended)
                .monthlyAppointments(monthlyAppointments)
                .monthlyCancellations(monthlyCancellations)
                .build();
    }

    private String buildBaseFilter(UUID clinicId, UUID doctorId) {
        StringBuilder sb = new StringBuilder("1 = 1");
        if (clinicId != null) {
            sb.append(" and clinic.id = :clinicId");
        }
        if (doctorId != null) {
            sb.append(" and doctor.id = :doctorId");
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

    private long countDistinctPatients(String baseFilter, Map<String, Object> params) {
        String jpql = "select count(distinct m.patient.id) from MedicalAppointmentEntity m where "
                + baseFilter.replace("clinic.", "m.clinic.").replace("doctor.", "m.doctor.")
                + " and m.appointmentDate >= :startOfWeek and m.appointmentDate < :endOfWeek and m.status = :status";
        var query = medicalAppointmentRepository.getEntityManager().createQuery(jpql, Long.class);
        params.forEach(query::setParameter);
        return query.getSingleResult();
    }

    private long countAppointments(String query, Map<String, Object> params) {
        return medicalAppointmentRepository.count(query, params);
    }

    private Map<String, Object> merge(Map<String, Object> base, Map<String, Object> extra) {
        Map<String, Object> merged = new HashMap<>(base);
        merged.putAll(extra);
        return merged;
    }
}
