package com.clinic.dto.staff;

import java.time.LocalDate;
import java.time.LocalTime;

import com.clinic.common.enums.ScheduleStatus;

import lombok.Data;

@Data
public class StaffScheduleResponse {
    private Integer scheduleId;
    private Integer staffId;
    private String staffName;
    private LocalDate workingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private ScheduleStatus status;
    private String note;
}