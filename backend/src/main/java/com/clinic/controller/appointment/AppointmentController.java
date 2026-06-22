package com.clinic.controller.appointment;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.dto.appointment.AppointmentFilterRequest;
import com.clinic.dto.appointment.AppointmentRequest;
import com.clinic.dto.appointment.AppointmentResponse;
import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.service.appointment.AppointmentService;
import com.clinic.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> getAll(@ModelAttribute AppointmentFilterRequest filter) {
        return ResponseUtil.success("Appointments retrieved successfully", appointmentService.getAll(filter));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAllLegacy() {
        return ResponseUtil.success("Appointments retrieved successfully", appointmentService.getAllActive());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> create(@Valid @RequestBody AppointmentRequest request) {
        return ResponseUtil.success("Appointment created successfully", appointmentService.create(request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable Integer id,
            @RequestParam AppointmentStatus status) {
        return ResponseUtil.success("Appointment status updated successfully", appointmentService.updateStatus(id, status));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelByPatient(
            @PathVariable Integer id,
            @RequestParam String reason) {
        return ResponseUtil.success("Appointment cancelled successfully", appointmentService.cancelByPatient(id, reason));
    }

    @PatchMapping("/{id}/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> transferDoctor(
            @PathVariable Integer id,
            @RequestParam Integer newDoctorId) {
        return ResponseUtil.success("Doctor transferred successfully", appointmentService.transferDoctor(id, newDoctorId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getMyAppointments() {
        return ResponseUtil.success("My appointments retrieved successfully", appointmentService.getMyAppointments());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getDetail(@PathVariable Integer id) {
        return ResponseUtil.success("Appointment detail retrieved successfully", appointmentService.getDetail(id));
    }

    @GetMapping("/slots")
    public ResponseEntity<ApiResponse<List<com.clinic.dto.appointment.TimeSlotResponse>>> getAvailableSlots(
            @RequestParam(required = false) Integer doctorId,
            @RequestParam(required = false) Integer expertiseId,
            @RequestParam(required = false) Integer serviceId,
            @RequestParam java.time.LocalDate date) {
        return ResponseUtil.success(
                "Time slots retrieved successfully",
                appointmentService.getAvailableSlots(doctorId, expertiseId, serviceId, date));
    }
}