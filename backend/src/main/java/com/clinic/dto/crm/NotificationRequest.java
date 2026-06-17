package com.clinic.dto.crm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotNull(message = "Type is required")
    private String type; // EMAIL or SYSTEM

    @NotBlank(message = "Content is required")
    private String content;

    private Integer accountId; // Optional, if null => send to all
}