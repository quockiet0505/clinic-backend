package com.clinic.dto.staff;

import java.time.LocalDate;
import java.time.LocalTime;

import com.clinic.common.enums.ScheduleStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StaffScheduleRequest {
    
    @NotNull(message = "Staff ID is required")
    private Integer staffId;

    @NotNull(message = "Working date is required")
    private LocalDate workingDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Schedule status is required (WORKING or OFF)")
    private ScheduleStatus status;

    private String note;
}