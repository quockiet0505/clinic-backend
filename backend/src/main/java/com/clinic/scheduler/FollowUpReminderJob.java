package com.clinic.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.clinic.common.enums.FollowUpStatus;
import com.clinic.entity.crm.FollowUp;
import com.clinic.repository.crm.FollowUpRepository;
import com.clinic.service.crm.FollowUpService;
import com.clinic.service.crm.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FollowUpReminderJob {

    private static final DateTimeFormatter DISPLAY_DATETIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final FollowUpRepository followUpRepository;
    private final FollowUpService followUpService;
    private final NotificationService notificationService;

    /** Nhắc tái khám trước ~24h — chạy 09:00 mỗi ngày. */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendFollowUpReminders() {
        log.info("Starting daily follow-up reminder job...");

        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime endOfTomorrow = startOfTomorrow.plusDays(1).minusSeconds(1);

        List<FollowUpStatus> targetStatuses = Arrays.asList(
                FollowUpStatus.PENDING,
                FollowUpStatus.CONFIRMED
        );

        List<FollowUp> followUps = followUpRepository.findDueForReminder(
                startOfTomorrow, endOfTomorrow, targetStatuses);

        for (FollowUp followUp : followUps) {
            if (followUp.getPatient().getAccount() == null) {
                continue;
            }

            Integer accountId = followUp.getPatient().getAccount().getAccountId();
            String content = String.format(
                    "Nhắc tái khám: Bạn có lịch tái khám vào %s với Bác sĩ %s. Vui lòng đến đúng giờ hoặc xác nhận trên ứng dụng.",
                    followUp.getScheduledDatetime().format(DISPLAY_DATETIME),
                    followUp.getDoctor().getFullName()
            );

            notificationService.createAndSendNotification(accountId, content, "SYSTEM");
            followUpService.markReminderSent(followUp.getFollowUpId());

            log.info("Follow-up reminder sent to patient ID: {}", followUp.getPatient().getPatientId());
        }

        log.info("Finished follow-up reminder job. Sent {} reminders.", followUps.size());
    }
}
