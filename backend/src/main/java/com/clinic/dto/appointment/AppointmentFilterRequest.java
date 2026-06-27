// src/main/java/com/clinic/dto/appointment/AppointmentFilterRequest.java
package com.clinic.dto.appointment;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.AppointmentType;
import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppointmentFilterRequest extends BaseFilterRequest {
    private AppointmentStatus status;
    private AppointmentType appointmentType;
    private Integer doctorId;
    private Integer patientId;
    private String source; // ONLINE, WALK_IN
    private String serviceType; // EXAM, LAB_TEST, X_RAY, ULTRASOUND
    private String tab; // all, today, upcoming, queue
}