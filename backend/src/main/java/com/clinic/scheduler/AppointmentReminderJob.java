package com.clinic.scheduler;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.NotificationType;
import com.clinic.entity.appointment.Appointment;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.service.notification.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderJob {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    // Runs every day at 08:00 AM automatically
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendAppointmentReminders() {
        log.info("Starting Daily Appointment Reminder Job...");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        // Find all active appointments scheduled for tomorrow
        List<Appointment> appointmentsTomorrow = appointmentRepository.findByIsDeleted(0).stream()
                .filter(app -> app.getAppointmentDate().equals(tomorrow))
                .filter(app -> app.getStatus() == AppointmentStatus.CONFIRMED || app.getStatus() == AppointmentStatus.PENDING)
                .collect(Collectors.toList());

        for (Appointment appointment : appointmentsTomorrow) {
            // Only send if the patient has a registered account
            if (appointment.getPatient().getAccount() != null) {
                Integer accountId = appointment.getPatient().getAccount().getAccountId();
                String subject = "Reminder: Upcoming Clinic Appointment Tomorrow";
                String content = String.format(
                        "Dear %s, this is a reminder for your appointment with Dr. %s tomorrow (%s) at %s. Please arrive 15 minutes early.",
                        appointment.getPatient().getFullName(),
                        appointment.getMainDoctor().getFullName(),
                        appointment.getAppointmentDate().toString(),
                        appointment.getTimeStart() != null ? appointment.getTimeStart().toString() : "TBD"
                );

                notificationService.createAndSendNotification(accountId, subject, content, NotificationType.EMAIL);
                log.info("Reminder sent to Patient ID: {}", appointment.getPatient().getPatientId());
            }
        }
        
        log.info("Finished Appointment Reminder Job.");
    }
}