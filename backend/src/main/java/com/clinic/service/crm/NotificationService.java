package com.clinic.service.crm;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.NotificationType;
import com.clinic.dto.crm.NotificationResponse;
import com.clinic.entity.auth.Account;
import com.clinic.entity.crm.Notification;
import com.clinic.mapper.crm.NotificationMapper;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.crm.NotificationRepository;
import com.clinic.util.EmailUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;
    private final NotificationMapper notificationMapper;
    private final EmailUtil emailUtil;

    // Internal method used by Schedulers to create and send notifications
    @Transactional
    public void createAndSendNotification(Integer accountId, String subject, String content, NotificationType type) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            log.warn("Account ID {} not found. Cannot send notification.", accountId);
            return;
        }

        Notification notification = new Notification();
        notification.setAccount(account);
        notification.setType(type);
        notification.setContent(content);
        notificationRepository.save(notification);

        // If it's an email notification, trigger the EmailUtil to actually send it
        if (type == NotificationType.EMAIL && account.getEmail() != null) {
            emailUtil.sendEmail(account.getEmail(), subject, content);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Integer accountId) {
        return notificationRepository.findByAccount_AccountIdOrderBySentAtDesc(accountId)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }
}