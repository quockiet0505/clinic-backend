package com.clinic.controller.appointment;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.dto.appointment.AppointmentFilterRequest;
import com.clinic.dto.appointment.AppointmentRequest;
import com.clinic.dto.appointment.AppointmentResponse;
import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.service.appointment.AppointmentService;
import com.clinic.service.appointment.AppointmentQueueService;
import com.clinic.service.appointment.AppointmentSlotService;
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
    private final AppointmentQueueService appointmentQueueService;
    private final AppointmentSlotService appointmentSlotService;

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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseUtil.success("Appointment updated successfully", appointmentService.updateAppointment(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable Integer id,
            @RequestParam AppointmentStatus status,
            @RequestParam(required = false, defaultValue = "false") Boolean isPriority) {
        return ResponseUtil.success("Appointment status updated successfully", appointmentService.updateStatus(id, status, isPriority));
    }

    @PatchMapping("/{id}/queue/call")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> callPatient(@PathVariable Integer id) {
        return ResponseUtil.success("Patient called successfully", appointmentQueueService.callPatient(id));
    }

    @PatchMapping("/{id}/queue/skip")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> skipPatient(@PathVariable Integer id) {
        return ResponseUtil.success("Patient skipped", appointmentQueueService.skipPatient(id));
    }

    @PatchMapping("/{id}/queue/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> returnToQueue(@PathVariable Integer id) {
        return ResponseUtil.success("Patient returned to queue", appointmentQueueService.returnToQueue(id));
    }

    @PatchMapping("/{id}/queue/send-to-lab")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> sendToLab(@PathVariable Integer id) {
        return ResponseUtil.success("Patient sent to lab", appointmentQueueService.sendToLab(id));
    }

    @PatchMapping("/{id}/queue/return-from-lab")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> returnFromLab(@PathVariable Integer id) {
        return ResponseUtil.success("Patient returned from lab (Re-exam)", appointmentQueueService.returnFromLab(id));
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
                appointmentSlotService.getAvailableSlots(doctorId, expertiseId, serviceId, date));
    }
}