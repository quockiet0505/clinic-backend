package com.clinic.dto.appointment;

import java.time.LocalDate;
import java.time.LocalTime;

import com.clinic.common.enums.AppointmentType;
import com.clinic.common.enums.BookingMode;
import com.clinic.common.enums.CreatedByType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRequest {

    private Integer patientId;

    private Integer mainDoctorId;

    private Integer expertiseId;

    private Integer suggestedExpertiseId;

    private Integer serviceId;

    private BookingMode bookingMode;

    private Boolean isAiSuggested;

    private Boolean isPriority;

    @NotNull
    private LocalDate appointmentDate;

    @NotNull
    private LocalTime timeStart;

    @NotNull
    private LocalTime timeEnd;

    @NotNull
    private AppointmentType appointmentType;

    @NotNull
    private CreatedByType createdBy;

    private String note;
}
