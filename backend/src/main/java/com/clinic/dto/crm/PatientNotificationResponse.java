package com.clinic.dto.crm;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientNotificationResponse {
    private Integer id;
    private String type;
    private String subject;
    private String content;
    private LocalDateTime sentAt;
}
