package com.clinic.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatResponse {
    private Integer doctorId;
    private String doctorName;
    private Long totalAppointments;
    private Long completedAppointments;
    private Double completionRate; // %
    private Double revenue;
    private Double avgRating;
    private String imageUrl;
}