package com.clinic.dto.staff;

import com.clinic.common.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewLeaveRequestRequest {
    @NotNull(message = "Status is required")
    private LeaveStatus status;
    
    private String rejectionReason;
}
