package com.clinic.dto.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.AppointmentType;
import com.clinic.common.enums.CancelledByType;
import com.clinic.common.enums.CreatedByType;

import lombok.Data;

@Data
public class AppointmentResponse {
    private Integer appointmentId;
    private Integer patientId;
    private String patientName;
    private Integer mainDoctorId;
    private String doctorName;
    private LocalDate appointmentDate;
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private AppointmentType appointmentType;
    private AppointmentStatus status;
    private CreatedByType createdBy;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private Integer queueNumber;
    
    // New fields mapped from DB
    private CancelledByType cancelledBy;
    private String cancelReason;
}