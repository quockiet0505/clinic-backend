package com.clinic.dto.feedback;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FeedbackResponse {
    private Integer feedbackId;
    private Integer recordId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}