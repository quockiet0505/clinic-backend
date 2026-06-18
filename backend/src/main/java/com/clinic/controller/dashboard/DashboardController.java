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
    public ResponseEntity<ApiResponse<List<DoctorStatResponse>>> getDoctorStats(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseUtil.success("Success", dashboardService.getDoctorStats(month, year));
    }

    @GetMapping("/service-stats")
    public ResponseEntity<ApiResponse<List<ServiceStatResponse>>> getServiceStats(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseUtil.success("Success", dashboardService.getServiceStats(month, year));
    }

    @GetMapping("/patient-stats")
    public ResponseEntity<ApiResponse<PatientStatsResponse>> getPatientStats(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseUtil.success("Success", dashboardService.getPatientStats(month, year));
    }

    @GetMapping("/revenue-stats")
    public ResponseEntity<ApiResponse<RevenueStatsResponse>> getRevenueStats(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseUtil.success("Success", dashboardService.getRevenueStats(month, year));
    }

    //  Endpoint 1: Lấy thống kê 12 tháng cho biểu đồ
    @GetMapping("/monthly-stats")
    public ResponseEntity<ApiResponse<List<MonthlyStatResponse>>> getMonthlyStats(
            @RequestParam int year) {
        return ResponseUtil.success("Success", dashboardService.getMonthlyStats(year));
    }

    //  Endpoint 2: Lấy danh sách lịch hẹn gần đây
    @GetMapping("/recent-appointments")
    public ResponseEntity<ApiResponse<List<RecentAppointmentResponse>>> getRecentAppointments(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseUtil.success("Success", dashboardService.getRecentAppointments(limit));
    }

    //  Endpoint 3: Xem trước nội dung báo cáo
    @PostMapping("/report-preview")
    public ResponseEntity<ApiResponse<String>> previewReport(@RequestBody ReportFilterRequest filter) {
        String preview = dashboardService.generateReportPreview(filter);
        return ResponseUtil.success("Preview generated", preview);
    }

    //  Endpoint 4: Xuất file báo cáo PDF
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