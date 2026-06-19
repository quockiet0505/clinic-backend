package com.clinic.controller.medical;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.medical.ServiceOrderFilterRequest;
import com.clinic.dto.medical.ServiceOrderRequest;
import com.clinic.dto.medical.ServiceOrderResponse;
import com.clinic.service.medical.ServiceOrderService;
import com.clinic.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ApiResponse<ServiceOrderResponse> create(
            @Valid @RequestBody ServiceOrderRequest request
    ) {
        return ResponseUtil.success(
                "Service order created successfully",
                serviceOrderService.create(request)
        ).getBody();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'LAB_TECH')")
    public ApiResponse<PageResponse<ServiceOrderResponse>> getAll(
            @ModelAttribute ServiceOrderFilterRequest filter
    ) {
        return ResponseUtil.success(
                "Service orders fetched successfully",
                serviceOrderService.getAll(filter)
        ).getBody();
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'LAB_TECH')")
    public ApiResponse<List<ServiceOrderResponse>> getAllLegacy() {
        return ResponseUtil.success(
                "Service orders fetched successfully",
                serviceOrderService.getAll()
        ).getBody();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'LAB_TECH')")
    public ApiResponse<ServiceOrderResponse> updateStatus(
            @PathVariable Integer id,
            @RequestParam ServiceOrderStatus status,
            @RequestParam(required = false) Integer actionStaffId,
            @RequestParam(required = false) String rejectionReason
    ) {

        return ResponseUtil.success(
                "Service order updated successfully",
                serviceOrderService.updateStatus(id, status, actionStaffId, rejectionReason)
        ).getBody();
    }
}