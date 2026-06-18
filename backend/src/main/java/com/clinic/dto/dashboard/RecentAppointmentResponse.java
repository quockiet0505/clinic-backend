// src/main/java/com/clinic/dto/dashboard/RecentAppointmentResponse.java
package com.clinic.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecentAppointmentResponse {
    private Integer appointmentId;
    private String patientName;
    private String appointmentDate;
    private String status;
    private String patientAvatarUrl;
}