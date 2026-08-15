package com.clinic.dto.appointment;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRescheduleRequest {

    @NotNull(message = "Vui lòng chọn ngày khám")
    private LocalDate appointmentDate;

    @NotNull(message = "Vui lòng chọn giờ khám")
    private LocalTime timeStart;

    @NotNull
    private LocalTime timeEnd;

    private Integer mainDoctorId;

    @jakarta.validation.constraints.NotBlank(message = "Vui lòng nhập lý do dời lịch")
    private String rescheduleReason;
}
