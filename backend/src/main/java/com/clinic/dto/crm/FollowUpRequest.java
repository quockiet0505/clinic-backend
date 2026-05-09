package com.clinic.dto.crm;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FollowUpRequest {
    @NotNull(message = "Record ID is required")
    private Integer recordId;

    @NotNull(message = "Patient ID is required")
    private Integer patientId;

    @NotNull(message = "Doctor ID is required")
    private Integer doctorId;

    @NotNull(message = "Scheduled datetime is required")
    @Future(message = "Follow up date must be in the future")
    private LocalDateTime scheduledDatetime;

    private String note;
}