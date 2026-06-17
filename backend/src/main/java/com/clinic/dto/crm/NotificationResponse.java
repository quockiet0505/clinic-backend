package com.clinic.dto.crm;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Integer notificationId;
    private Integer accountId;
    private String accountName;
    private String type;
    private String content;
    private LocalDateTime sentAt;
}