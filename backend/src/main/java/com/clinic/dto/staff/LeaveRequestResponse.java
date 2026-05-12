package com.clinic.dto.staff;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.clinic.common.enums.LeaveStatus;
import com.clinic.common.enums.LeaveType;

import lombok.Data;

@Data
public class LeaveRequestResponse {
    private Integer leaveId;
    private Integer staffId;
    private String staffName;
    private LeaveType leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;
    private LeaveStatus status;
    private Integer approvedById;
    private String approvedByName;
    private String rejectionReason;
    private LocalDateTime reviewedAt;
}