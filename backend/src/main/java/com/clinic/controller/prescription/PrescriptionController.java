package com.clinic.controller.prescription;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.prescription.PrescriptionRequest;
import com.clinic.dto.prescription.PrescriptionResponse;
import com.clinic.service.prescription.PrescriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    /**
     * Create a new prescription for a patient after a medical examination.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<PrescriptionResponse> create(@Valid @RequestBody PrescriptionRequest request) {
        return ResponseEntity.ok(prescriptionService.create(request));
    }

    /**
     * Get prescription details by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<PrescriptionResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(prescriptionService.getById(id));
    }

    /**
     * List all prescriptions in the system.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<List<PrescriptionResponse>> getAll() {
        return ResponseEntity.ok(prescriptionService.getAll());
    }
}