package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.response.NotificationDto;
import gt.com.xfactory.entity.MedicalAppointmentEntity;
import gt.com.xfactory.entity.enums.AppointmentStatus;
import gt.com.xfactory.repository.MedicalAppointmentRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

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

        List<MedicalAppointmentEntity> overdue = medicalAppointmentRepository
                .find("status IN (:statuses) AND appointmentDate < :cutoff",
                        java.util.Map.of(
                                "statuses", List.of(AppointmentStatus.scheduled, AppointmentStatus.confirmed),
                                "cutoff", cutoff))
                .list();

        for (MedicalAppointmentEntity appointment : overdue) {
            appointment.setStatus(AppointmentStatus.expired);
            medicalAppointmentRepository.persist(appointment);
            log.info("Appointment {} expired", appointment.getId());

            notificationService.notify(appointment.getDoctor().getId(), NotificationDto.builder()
                    .type("APPOINTMENT_EXPIRED")
                    .appointmentId(appointment.getId())
                    .patientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName())
                    .appointmentDate(appointment.getAppointmentDate())
                    .message("La cita ha expirado automáticamente")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        if (!overdue.isEmpty()) {
            log.info("Expired {} overdue appointments", overdue.size());
        }
    }

    @Scheduled(every = "5m")
    @Transactional
    void notifyUpcomingAppointments() {
        LocalDateTime now = LocalDateTime.now();

        // 30 min warning
        LocalDateTime thirtyMinStart = now.plusMinutes(28);
        LocalDateTime thirtyMinEnd = now.plusMinutes(33);

        List<MedicalAppointmentEntity> thirtyMinAppts = medicalAppointmentRepository
                .find("status IN (:statuses) AND appointmentDate BETWEEN :start AND :end AND notified30Min = false",
                        java.util.Map.of(
                                "statuses", List.of(AppointmentStatus.scheduled, AppointmentStatus.confirmed),
                                "start", thirtyMinStart,
                                "end", thirtyMinEnd))
                .list();

        for (MedicalAppointmentEntity appointment : thirtyMinAppts) {
            appointment.setNotified30Min(true);
            medicalAppointmentRepository.persist(appointment);

            notificationService.notify(appointment.getDoctor().getId(), NotificationDto.builder()
                    .type("APPOINTMENT_EXPIRING")
                    .appointmentId(appointment.getId())
                    .patientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName())
                    .appointmentDate(appointment.getAppointmentDate())
                    .message("La cita expirará en 30 minutos")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        // 10 min warning
        LocalDateTime tenMinStart = now.plusMinutes(8);
        LocalDateTime tenMinEnd = now.plusMinutes(13);

        List<MedicalAppointmentEntity> tenMinAppts = medicalAppointmentRepository
                .find("status IN (:statuses) AND appointmentDate BETWEEN :start AND :end AND notified10Min = false",
                        java.util.Map.of(
                                "statuses", List.of(AppointmentStatus.scheduled, AppointmentStatus.confirmed),
                                "start", tenMinStart,
                                "end", tenMinEnd))
                .list();

        for (MedicalAppointmentEntity appointment : tenMinAppts) {
            appointment.setNotified10Min(true);
            medicalAppointmentRepository.persist(appointment);

            notificationService.notify(appointment.getDoctor().getId(), NotificationDto.builder()
                    .type("APPOINTMENT_EXPIRING")
                    .appointmentId(appointment.getId())
                    .patientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName())
                    .appointmentDate(appointment.getAppointmentDate())
                    .message("La cita expirará en 10 minutos")
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }
}
