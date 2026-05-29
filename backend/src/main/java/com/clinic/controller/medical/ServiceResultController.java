package com.clinic.controller.medical;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.medical.ServiceResultRequest;
import com.clinic.dto.medical.ServiceResultResponse;
import com.clinic.service.medical.ServiceResultService;
import com.clinic.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/service-results")
@RequiredArgsConstructor
public class ServiceResultController {

    private final ServiceResultService resultService;

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_TECH', 'DOCTOR')")
    public ApiResponse<ServiceResultResponse> submitResult(
            @Valid @RequestBody ServiceResultRequest request
    ) {

        return ResponseUtil.success(
                "Service result submitted successfully",
                resultService.submitResult(request)
        ).getBody();
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'LAB_TECH', 'PATIENT', 'STAFF')")
    public ApiResponse<ServiceResultResponse> getByOrderId(
            @PathVariable Integer orderId
    ) {

        return ResponseUtil.success(
                "Service result fetched successfully",
                resultService.getByOrderId(orderId)
        ).getBody();
    }
}