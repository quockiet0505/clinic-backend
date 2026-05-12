package com.clinic.dto.appointment;

import java.time.LocalDate;
import java.time.LocalTime;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.AppointmentType;
import com.clinic.common.enums.CancelledByType;
import com.clinic.common.enums.CreatedByType;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRequest {
    
    @NotNull(message = "Patient ID is required")
    private Integer patientId;

    @NotNull(message = "Doctor ID is required")
    private Integer mainDoctorId;

    @NotNull(message = "Appointment date is required")
    @FutureOrPresent(message = "Appointment date cannot be in the past")
    private LocalDate appointmentDate;

    private LocalTime timeStart;
    private LocalTime timeEnd;

    @NotNull(message = "Appointment type is required (ONLINE or WALK_IN)")
    private AppointmentType appointmentType;

    @NotNull(message = "Creator type is required (PATIENT or STAFF)")
    private CreatedByType createdBy;

    // Optional fields for update/cancel
    private AppointmentStatus status;
    private CancelledByType cancelledBy;
    private String cancelReason;
}