package com.clinic.dto.crm;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DoctorFeedbackResponse {
    private Integer reviewId;
    private Integer doctorId;
    private String doctorName;
    private Integer patientId;
    private String patientName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private String reply;
    private LocalDateTime repliedAt;
    private String repliedBy;
}