package com.clinic.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private Long totalPatients;
    private Long appointmentsToday;
    private Long appointmentsThisWeek;
    private Long completedAppointments;
    private Long cancelledAppointments;
    private Long noShowAppointments;
    private Long pendingAppointments;
    private Long totalAppointments;
    private Long totalStaff;
    private Long totalDoctors;
    private Long totalFeedbacks;
    private Double avgRating;
    private Double monthlyRevenue;
}