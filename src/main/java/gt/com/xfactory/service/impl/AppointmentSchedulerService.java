package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.response.NotificationDto;
import gt.com.xfactory.repository.MedicalAppointmentRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.util.*;
import java.util.stream.*;

@ApplicationScoped
@Slf4j
public class AppointmentSchedulerService {

    @Inject
    MedicalAppointmentRepository medicalAppointmentRepository;

    @Inject
    NotificationService notificationService;

    @Scheduled(every = "5m")
    @Transactional
    void expireOverdueAppointments() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = medicalAppointmentRepository.getEntityManager()
                .createNativeQuery(
                        "SELECT ma.id, ma.doctor_id, " +
                        "p.first_name || ' ' || p.last_name AS patient_name, ma.appointment_date " +
                        "FROM medical_appointment ma " +
                        "JOIN patient p ON ma.patient_id = p.id " +
                        "WHERE ma.status IN ('scheduled', 'confirmed') " +
                        "AND ma.appointment_date < :cutoff")
                .setParameter("cutoff", cutoff)
                .getResultList();

        if (rows.isEmpty()) return;

        List<UUID> ids = rows.stream()
                .map(row -> UUID.fromString(row[0].toString()))
                .collect(Collectors.toList());

        medicalAppointmentRepository.getEntityManager()
                .createNativeQuery("UPDATE medical_appointment SET status = 'expired' WHERE id IN (:ids)")
                .setParameter("ids", ids)
                .executeUpdate();

        for (Object[] row : rows) {
            UUID doctorId = UUID.fromString(row[1].toString());
            String patientName = (String) row[2];
            LocalDateTime date = toLocalDateTime(row[3]);

            notificationService.notify(doctorId, NotificationDto.builder()
                    .type("APPOINTMENT_EXPIRED")
                    .appointmentId(UUID.fromString(row[0].toString()))
                    .patientName(patientName)
                    .appointmentDate(date)
                    .message("La cita ha expirado automáticamente")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        log.info("Expired {} overdue appointments", rows.size());
    }

    @Scheduled(every = "5m")
    @Transactional
    void notifyUpcomingAppointments() {
        LocalDateTime now = LocalDateTime.now();
        processUpcomingWindow(now.plusMinutes(28), now.plusMinutes(33),
                "notified_30_min", "La cita expirará en 30 minutos");
        processUpcomingWindow(now.plusMinutes(8), now.plusMinutes(13),
                "notified_10_min", "La cita expirará en 10 minutos");
    }

    private void processUpcomingWindow(LocalDateTime start, LocalDateTime end,
                                       String flagColumn, String message) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = medicalAppointmentRepository.getEntityManager()
                .createNativeQuery(
                        "SELECT ma.id, ma.doctor_id, " +
                        "p.first_name || ' ' || p.last_name AS patient_name, ma.appointment_date " +
                        "FROM medical_appointment ma " +
                        "JOIN patient p ON ma.patient_id = p.id " +
                        "WHERE ma.status IN ('scheduled', 'confirmed') " +
                        "AND ma.appointment_date BETWEEN :start AND :end " +
                        "AND ma." + flagColumn + " = false")
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        if (rows.isEmpty()) return;

        List<UUID> ids = rows.stream()
                .map(row -> UUID.fromString(row[0].toString()))
                .collect(Collectors.toList());

        medicalAppointmentRepository.getEntityManager()
                .createNativeQuery("UPDATE medical_appointment SET " + flagColumn + " = true WHERE id IN (:ids)")
                .setParameter("ids", ids)
                .executeUpdate();

        for (Object[] row : rows) {
            UUID doctorId = UUID.fromString(row[1].toString());
            String patientName = (String) row[2];
            LocalDateTime date = toLocalDateTime(row[3]);

            notificationService.notify(doctorId, NotificationDto.builder()
                    .type("APPOINTMENT_EXPIRING")
                    .appointmentId(UUID.fromString(row[0].toString()))
                    .patientName(patientName)
                    .appointmentDate(date)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        throw new IllegalArgumentException("Cannot convert to LocalDateTime: " + value.getClass());
    }
}
