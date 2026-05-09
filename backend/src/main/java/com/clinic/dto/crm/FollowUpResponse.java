package com.clinic.dto.crm;

import java.time.LocalDateTime;

import com.clinic.common.enums.FollowUpStatus;

import lombok.Data;

@Data
public class FollowUpResponse {
    private Integer followUpId;
    private Integer recordId;
    private Integer patientId;
    private String patientName;
    private Integer doctorId;
    private String doctorName;
    private LocalDateTime scheduledDatetime;
    private String note;
    private FollowUpStatus status;
}