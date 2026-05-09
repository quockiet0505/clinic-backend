package com.clinic.scheduler;

import com.clinic.common.enums.FollowUpStatus;
import com.clinic.common.enums.NotificationType;
import com.clinic.entity.followup.FollowUp;
import com.clinic.repository.followup.FollowUpRepository;
import com.clinic.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FollowUpReminderJob {

    private final FollowUpRepository followUpRepository;
    private final NotificationService notificationService;

    // Runs every day at 09:00 AM automatically
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendFollowUpReminders() {
        log.info("Starting Daily Follow-up Reminder Job...");

        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime endOfTomorrow = startOfTomorrow.plusDays(1).minusSeconds(1);

        // Find all pending or confirmed follow-ups scheduled for tomorrow
        List<FollowUp> followUps = followUpRepository.findByScheduledDatetimeBetweenAndStatus(
                startOfTomorrow, endOfTomorrow, FollowUpStatus.PENDING);
                
        List<FollowUp> confirmedFollowUps = followUpRepository.findByScheduledDatetimeBetweenAndStatus(
                startOfTomorrow, endOfTomorrow, FollowUpStatus.CONFIRMED);
                
        followUps.addAll(confirmedFollowUps);

        for (FollowUp followUp : followUps) {
            if (followUp.getPatient().getAccount() != null) {
                Integer accountId = followUp.getPatient().getAccount().getAccountId();
                String subject = "Reminder: Scheduled Follow-up Visit Tomorrow";
                String content = String.format(
                        "Dear %s, you have a follow-up visit scheduled tomorrow at %s with Dr. %s. Please do not forget.",
                        followUp.getPatient().getFullName(),
                        followUp.getScheduledDatetime().toLocalTime().toString(),
                        followUp.getDoctor().getFullName()
                );

                notificationService.createAndSendNotification(accountId, subject, content, NotificationType.EMAIL);
                log.info("Follow-up Reminder sent to Patient ID: {}", followUp.getPatient().getPatientId());
            }
        }

        log.info("Finished Follow-up Reminder Job.");
    }
}