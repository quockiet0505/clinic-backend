package com.clinic.service.crm;

import com.clinic.common.enums.NotificationType; // import enum
import com.clinic.dto.crm.NotificationResponse;
import com.clinic.dto.crm.NotificationRequest;
import com.clinic.entity.auth.Account;
import com.clinic.entity.crm.Notification;
import com.clinic.mapper.crm.NotificationMapper;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.crm.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;
    private final NotificationMapper notificationMapper;

    public List<NotificationResponse> getNotifications(String search, String type, String fromDate, String toDate) {
        List<Notification> notifications = notificationRepository.findAll();

        if (type != null && !type.isEmpty() && !type.equals("ALL")) {
            notifications = notifications.stream()
                    .filter(n -> n.getType().name().equals(type))
                    .collect(Collectors.toList());
        }

        if (fromDate != null && !fromDate.isEmpty() || toDate != null && !toDate.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate from = (fromDate != null && !fromDate.isEmpty()) ? LocalDate.parse(fromDate, formatter) : null;
            LocalDate to = (toDate != null && !toDate.isEmpty()) ? LocalDate.parse(toDate, formatter) : null;

            notifications = notifications.stream()
                    .filter(n -> {
                        LocalDate sentDate = n.getSentAt().toLocalDate();
                        if (from != null && sentDate.isBefore(from)) return false;
                        if (to != null && sentDate.isAfter(to)) return false;
                        return true;
                    })
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            notifications = notifications.stream()
                    .filter(n -> {
                        String accountName = n.getAccount() != null ? n.getAccount().getEmail() : "";
                        return accountName.toLowerCase().contains(searchLower) ||
                                n.getContent().toLowerCase().contains(searchLower);
                    })
                    .collect(Collectors.toList());
        }

        return notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createNotification(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setType(Notification.Type.valueOf(request.getType()));
        notification.setContent(request.getContent());
        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            notification.setAccount(account);
        }
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    // Overload cho scheduler (4 tham số)
    @Transactional
    public void createAndSendNotification(Integer accountId, String content, String type, NotificationType notificationType) {
        // Chuyển đổi NotificationType sang String rồi gọi method 3 tham số
        createAndSendNotification(accountId, content, notificationType.name());
    }

    // Method gốc (3 tham số)
    @Transactional
    public void createAndSendNotification(Integer accountId, String content, String type) {
        NotificationRequest request = new NotificationRequest();
        request.setAccountId(accountId);
        request.setContent(content);
        request.setType(type);
        createNotification(request);
    }
}