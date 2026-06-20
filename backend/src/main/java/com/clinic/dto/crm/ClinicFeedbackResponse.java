package com.clinic.dto.crm;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClinicFeedbackResponse {
    private Integer feedbackId;
    private Integer recordId;
    private String patientName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private String reply;
    private LocalDateTime repliedAt;
    private String repliedBy; // tên nhân viên phản hồi
    private Boolean isAnonymous;
}