package com.clinic.controller.medical;

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

import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.dto.medical.ServiceOrderRequest;
import com.clinic.dto.medical.ServiceOrderResponse;
import com.clinic.service.medical.ServiceOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    /**
     * Doctor creates a new service order (e.g., Blood Test, X-Ray)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ServiceOrderResponse> create(@Valid @RequestBody ServiceOrderRequest request) {
        return ResponseEntity.ok(serviceOrderService.create(request));
    }

    /**
     * Get all service orders in the system
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'LAB_TECH')")
    public ResponseEntity<List<ServiceOrderResponse>> getAll() {
        return ResponseEntity.ok(serviceOrderService.getAll());
    }

    /**
     * Update order status. Includes optional parameters for Lab Technicians 
     * to log who collected the sample or why the sample was rejected.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'LAB_TECH')")
    public ResponseEntity<ServiceOrderResponse> updateStatus(
            @PathVariable Integer id,
            @RequestParam ServiceOrderStatus status,
            @RequestParam(required = false) Integer actionStaffId,
            @RequestParam(required = false) String rejectionReason) {
        
        return ResponseEntity.ok(serviceOrderService.updateStatus(id, status, actionStaffId, rejectionReason));
    }
}