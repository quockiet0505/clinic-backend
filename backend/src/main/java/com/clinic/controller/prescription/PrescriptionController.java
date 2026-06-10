
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
import com.clinic.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ApiResponse<PrescriptionResponse> create(
            @Valid @RequestBody PrescriptionRequest request
    ) {
        return ResponseUtil.success(
                "Prescription created successfully",
                prescriptionService.create(request)
        ).getBody();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public ApiResponse<PrescriptionResponse> getById(@PathVariable Integer id) {
        return ResponseUtil.success(
                "Prescription fetched successfully",
                prescriptionService.getById(id)
        ).getBody();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ApiResponse<List<PrescriptionResponse>> getAll() {
        return ResponseUtil.success(
                "Prescriptions fetched successfully",
                prescriptionService.getAll()
        ).getBody();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<List<PrescriptionResponse>> getMyPrescriptions(org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        return ResponseUtil.success(
                "Prescriptions fetched successfully",
                prescriptionService.getMyPrescriptions(email)
        ).getBody();
    }
}

