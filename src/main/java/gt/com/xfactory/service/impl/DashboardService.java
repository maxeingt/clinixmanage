package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.response.DashboardDto;
import gt.com.xfactory.entity.enums.AppointmentStatus;
import gt.com.xfactory.repository.MedicalAppointmentRepository;
import gt.com.xfactory.repository.PatientRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
@Slf4j
public class DashboardService {

    @Inject
    MedicalAppointmentRepository medicalAppointmentRepository;

    @Inject
    PatientRepository patientRepository;

    public DashboardDto getDashboardMetrics(UUID clinicId, UUID doctorId) {
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

        long totalPatients = patientRepository.count();

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
                .totalPatients(totalPatients)
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

    private long countAppointments(String query, Map<String, Object> params) {
        return medicalAppointmentRepository.count(query, params);
    }

    private Map<String, Object> merge(Map<String, Object> base, Map<String, Object> extra) {
        Map<String, Object> merged = new HashMap<>(base);
        merged.putAll(extra);
        return merged;
    }
}
