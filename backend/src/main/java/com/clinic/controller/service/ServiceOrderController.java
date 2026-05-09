package com.clinic.controller.service;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.service.ServiceOrderRequest;
import com.clinic.dto.service.ServiceOrderResponse;
import com.clinic.service.service.ServiceOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController {
    private final ServiceOrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ServiceOrderResponse> create(@Valid @RequestBody ServiceOrderRequest request) {
        return ResponseEntity.ok(orderService.create(request));
    }

    @GetMapping("/record/{recordId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'LAB_TECH', 'STAFF', 'PATIENT')")
    public ResponseEntity<List<ServiceOrderResponse>> getByRecordId(@PathVariable Integer recordId) {
        return ResponseEntity.ok(orderService.getByRecordId(recordId));
    }
}