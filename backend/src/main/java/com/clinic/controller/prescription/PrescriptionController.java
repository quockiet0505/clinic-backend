package com.clinic.controller.prescription;

import com.clinic.dto.prescription.PrescriptionRequest;
import com.clinic.dto.prescription.PrescriptionResponse;
import com.clinic.service.prescription.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')") // Only doctors can prescribe medicine
    public ResponseEntity<PrescriptionResponse> create(@Valid @RequestBody PrescriptionRequest request) {
        return ResponseEntity.ok(prescriptionService.create(request));
    }

    @GetMapping("/record/{recordId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT', 'ADMIN')") // Multiple roles can view the prescription
    public ResponseEntity<PrescriptionResponse> getByRecordId(@PathVariable Integer recordId) {
        return ResponseEntity.ok(prescriptionService.getByRecordId(recordId));
    }
}