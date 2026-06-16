package com.clinic.controller.prescription;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.prescription.PrescriptionRequest;
import com.clinic.dto.prescription.PrescriptionResponse;
import com.clinic.service.prescription.PrescriptionService;
import org.springframework.web.bind.annotation.PutMapping;
import com.clinic.util.ResponseUtil;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    @PostMapping
    public ApiResponse<PrescriptionResponse> create(@Valid @RequestBody PrescriptionRequest request) {
        return ResponseUtil.success(
                "Prescription created", 
                prescriptionService.create(request)
        ).getBody(); 
    }

    @GetMapping("/{id}")
    public ApiResponse<PrescriptionResponse> getById(@PathVariable Integer id) {
        return ResponseUtil.success(
                "Prescription fetched", 
                prescriptionService.getById(id)
        ).getBody(); 
    }

    @GetMapping
    public ApiResponse<List<PrescriptionResponse>> getAll() {
        return ResponseUtil.success(
                "All prescriptions", 
                prescriptionService.getAll()
        ).getBody(); 
    }

    @PutMapping("/{id}/dispense")
    public ApiResponse<Void> dispense(@PathVariable Integer id) {
        prescriptionService.dispense(id);
        return ResponseUtil.success(
                "Prescription dispensed", 
                (Void) null
        ).getBody(); 
    }
}