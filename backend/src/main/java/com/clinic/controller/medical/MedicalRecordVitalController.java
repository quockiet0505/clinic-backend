package com.clinic.controller.medical;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.medical.MedicalRecordVitalRequest;
import com.clinic.dto.medical.MedicalRecordVitalResponse;
import com.clinic.service.medical.MedicalRecordVitalService;
import com.clinic.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/medical-vitals")
@RequiredArgsConstructor
public class MedicalRecordVitalController {

    private final MedicalRecordVitalService vitalService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'DOCTOR', 'ADMIN')")
    public ApiResponse<MedicalRecordVitalResponse> saveOrUpdate(
            @Valid @RequestBody MedicalRecordVitalRequest request
    ) {

        return ResponseUtil.success(
                "Medical vital saved successfully",
                vitalService.saveOrUpdate(request)
        ).getBody();
    }

    @GetMapping("/record/{recordId}")
    @PreAuthorize("hasAnyRole('STAFF', 'DOCTOR', 'ADMIN', 'PATIENT')")
    public ApiResponse<MedicalRecordVitalResponse> getByRecordId(
            @PathVariable Integer recordId
    ) {
        return ResponseUtil.success(
                "Medical vital fetched successfully",
                vitalService.getByRecordId(recordId)
        ).getBody();
    }

    @GetMapping("/my/latest")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<MedicalRecordVitalResponse> getMyLatestVitals(Authentication authentication) {
        return ResponseUtil.success(
                "Latest vitals fetched successfully",
                vitalService.getLatestByEmail(authentication.getName())
        ).getBody();
    }
}