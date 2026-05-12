package com.clinic.controller.appointment;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.dto.appointment.AppointmentRequest;
import com.clinic.dto.appointment.AppointmentResponse;
import com.clinic.service.appointment.AppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // Retrieve all active appointments
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> getAll() {
        return ResponseEntity.ok(appointmentService.getAllActive());
    }

    // Create a new appointment (Online booking or Walk-in)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'PATIENT')")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.create(request));
    }

    // Staff or Doctor updates the status (e.g., CHECKED_IN, COMPLETED, WAITING_RESULT)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Integer id, 
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }

    // Patient cancels the appointment (System checks if < 3 hours to mark as SPAM)
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'PATIENT')")
    public ResponseEntity<AppointmentResponse> cancelByPatient(
            @PathVariable Integer id,
            @RequestParam String reason) {
        return ResponseEntity.ok(appointmentService.cancelByPatient(id, reason));
    }

    // Staff transfers the appointment to another available doctor
    @PatchMapping("/{id}/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<AppointmentResponse> transferDoctor(
            @PathVariable Integer id,
            @RequestParam Integer newDoctorId) {
        return ResponseEntity.ok(appointmentService.transferDoctor(id, newDoctorId));
    }
}