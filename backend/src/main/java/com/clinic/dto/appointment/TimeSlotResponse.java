package com.clinic.dto.appointment;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotResponse {
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private boolean isAvailable;
    /** Bác sĩ gán cho slot (khi tra cứu theo chuyên khoa). */
    private Integer doctorId;
    private String doctorName;
}
