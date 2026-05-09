package com.clinic.dto.notification;

import java.time.LocalDateTime;

import com.clinic.common.enums.NotificationType;

import lombok.Data;

@Data
public class NotificationResponse {
    private Integer notificationId;
    private Integer accountId;
    private NotificationType type;
    private String content;
    private LocalDateTime sentAt;
}