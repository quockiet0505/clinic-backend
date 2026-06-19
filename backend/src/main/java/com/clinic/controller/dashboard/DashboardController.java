// src/main/java/com/clinic/controller/dashboard/DashboardController.java
package com.clinic.controller.dashboard;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.dashboard.*;
import com.clinic.service.dashboard.DashboardService;
import com.clinic.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        return ResponseUtil.success("Success", dashboardService.getStats());
    }

    @GetMapping("/doctor-stats")
    public ResponseEntity<ApiResponse<DoctorStatsPageResponse>> getDoctorStats(
            @ModelAttribute DashboardPeriodFilterRequest filter) {
        return ResponseUtil.success("Success", dashboardService.getDoctorStats(filter));
    }

    @GetMapping("/service-stats")
    public ResponseEntity<ApiResponse<ServiceStatsPageResponse>> getServiceStats(
            @ModelAttribute DashboardPeriodFilterRequest filter) {
        return ResponseUtil.success("Success", dashboardService.getServiceStats(filter));
    }

    @GetMapping("/patient-stats")
    public ResponseEntity<ApiResponse<PatientStatsResponse>> getPatientStats(
            @ModelAttribute DashboardPeriodFilterRequest filter) {
        return ResponseUtil.success("Success", dashboardService.getPatientStats(filter));
    }

    @GetMapping("/revenue-stats")
    public ResponseEntity<ApiResponse<RevenueStatsResponse>> getRevenueStats(
            @ModelAttribute DashboardPeriodFilterRequest filter) {
        return ResponseUtil.success("Success", dashboardService.getRevenueStats(filter));
    }

    @GetMapping("/monthly-stats")
    public ResponseEntity<ApiResponse<List<MonthlyStatResponse>>> getMonthlyStats(
            @RequestParam int year) {
        return ResponseUtil.success("Success", dashboardService.getMonthlyStats(year));
    }

    @GetMapping("/recent-appointments")
    public ResponseEntity<ApiResponse<List<RecentAppointmentResponse>>> getRecentAppointments(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseUtil.success("Success", dashboardService.getRecentAppointments(limit));
    }

    @PostMapping("/report-preview")
    public ResponseEntity<ApiResponse<String>> previewReport(@RequestBody ReportFilterRequest filter) {
        String preview = dashboardService.generateReportPreview(filter);
        return ResponseUtil.success("Preview generated", preview);
    }

    @PostMapping("/report")
    public ResponseEntity<byte[]> generateReport(@RequestBody ReportFilterRequest filter) {
        byte[] reportData = dashboardService.generateReport(filter);
        HttpHeaders headers = new HttpHeaders();
        String filename = "report_" + System.currentTimeMillis() + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentType(MediaType.APPLICATION_PDF);
        return new ResponseEntity<>(reportData, headers, HttpStatus.OK);
    }
}
