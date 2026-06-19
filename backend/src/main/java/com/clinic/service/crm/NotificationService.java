package com.clinic.service.crm;

import com.clinic.common.enums.NotificationType;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.crm.NotificationFilterRequest;
import com.clinic.dto.crm.NotificationResponse;
import com.clinic.dto.crm.NotificationRequest;
import com.clinic.entity.auth.Account;
import com.clinic.entity.crm.Notification;
import com.clinic.mapper.crm.NotificationMapper;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.crm.NotificationRepository;
import com.clinic.specification.crm.NotificationSpecification;
import com.clinic.util.FilterUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getAll(NotificationFilterRequest filter) {
        Specification<Notification> spec = NotificationSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<Notification> page = notificationRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(notificationMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllLegacy() {
        return notificationRepository.findAll().stream()
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

    @Transactional
    public void createAndSendNotification(Integer accountId, String content, String type, NotificationType notificationType) {
        createAndSendNotification(accountId, content, notificationType.name());
    }

    @Transactional
    public void createAndSendNotification(Integer accountId, String content, String type) {
        NotificationRequest request = new NotificationRequest();
        request.setAccountId(accountId);
        request.setContent(content);
        request.setType(type);
        createNotification(request);
    }
}
