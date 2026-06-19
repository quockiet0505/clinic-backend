package com.clinic.controller.medical;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.medical.MedicalRecordDetailResponse;
import com.clinic.dto.medical.MedicalRecordFilterRequest;
import com.clinic.dto.medical.MedicalRecordRequest;
import com.clinic.dto.medical.MedicalRecordResponse;
import com.clinic.service.medical.MedicalRecordService;
import com.clinic.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ApiResponse<MedicalRecordResponse> create(
            @Valid @RequestBody MedicalRecordRequest request
    ) {
        return ResponseUtil.success(
                "Medical record created successfully",
                medicalRecordService.create(request)
        ).getBody();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ApiResponse<PageResponse<MedicalRecordResponse>> getAll(
            @ModelAttribute MedicalRecordFilterRequest filter
    ) {
        return ResponseUtil.success(
                "Medical records fetched successfully",
                medicalRecordService.getAll(filter)
        ).getBody();
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ApiResponse<List<MedicalRecordResponse>> getAllLegacy() {
        return ResponseUtil.success(
                "Medical records fetched successfully",
                medicalRecordService.getAll()
        ).getBody();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ApiResponse<MedicalRecordResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody MedicalRecordRequest request
    ) {
        return ResponseUtil.success(
                "Medical record updated successfully",
                medicalRecordService.update(id, request)
        ).getBody();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<List<MedicalRecordResponse>> getMyRecords() {
        return ResponseUtil.success(
                "My medical records fetched successfully",
                medicalRecordService.getMyRecords()
        ).getBody();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public ApiResponse<MedicalRecordDetailResponse> getRecordDetail(
            @PathVariable Integer id
    ) {
        return ResponseUtil.success(
                "Medical record detail fetched successfully",
                medicalRecordService.getRecordDetail(id)
        ).getBody();
    }
}