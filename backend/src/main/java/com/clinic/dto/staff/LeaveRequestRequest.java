package com.clinic.dto.staff;

import java.time.LocalDate;
import com.clinic.common.enums.LeaveType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaveRequestRequest {
    @NotNull
    private Integer staffId;
    @NotNull
    private LeaveType leaveType;
    @NotNull
    private LocalDate fromDate;
    @NotNull
    private LocalDate toDate;
    
    private String reason;
}