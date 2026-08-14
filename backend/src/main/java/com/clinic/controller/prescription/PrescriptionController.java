package com.clinic.controller.prescription;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.prescription.PrescriptionFilterRequest;
import com.clinic.dto.prescription.PrescriptionRequest;
import com.clinic.dto.prescription.PrescriptionResponse;
import com.clinic.dto.prescription.DrugInteractionWarning;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ApiResponse<PrescriptionResponse> create(@Valid @RequestBody PrescriptionRequest request) {
        return ResponseUtil.success(
                "Prescription created", 
                prescriptionService.create(request)
        ).getBody(); 
    }

    @GetMapping
    public ApiResponse<PageResponse<PrescriptionResponse>> getAll(
            @ModelAttribute PrescriptionFilterRequest filter
    ) {
        return ResponseUtil.success(
                "All prescriptions",
                prescriptionService.getAll(filter)
        ).getBody();
    }

    @GetMapping("/all")
    public ApiResponse<List<PrescriptionResponse>> getAllLegacy() {
        return ResponseUtil.success(
                "All prescriptions",
                prescriptionService.getAll()
        ).getBody();
    }

    @GetMapping("/{id}")
    public ApiResponse<PrescriptionResponse> getById(@PathVariable Integer id) {
        return ResponseUtil.success(
                "Prescription fetched", 
                prescriptionService.getById(id)
        ).getBody(); 
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<List<PrescriptionResponse>> getMyPrescriptions(org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        return ResponseUtil.success(
                "My prescriptions fetched",
                prescriptionService.getMyPrescriptions(email)
        ).getBody();
    }


    @PostMapping("/check-interactions")
    public ApiResponse<List<DrugInteractionWarning>> checkInteractions(@RequestBody List<Integer> medicineIds) {
        List<DrugInteractionWarning> warnings = prescriptionService.checkInteractions(medicineIds);
        String message = warnings.isEmpty() ? "An toàn, không phát hiện tương tác thuốc" : "Phát hiện có tương tác thuốc!";
        return ResponseUtil.success(message, warnings).getBody();
    }
}