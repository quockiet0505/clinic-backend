package com.clinic.controller.medical;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.medical.ServiceRequest;
import com.clinic.dto.medical.ServiceResponse;
import com.clinic.service.medical.ServiceService;
import com.clinic.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping
    public ApiResponse<List<ServiceResponse>> getAll() {
        return ResponseUtil.success(
                "Services fetched successfully",
                serviceService.getAllActive()
        ).getBody();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ServiceResponse> create(
            @Valid @RequestBody ServiceRequest request
    ) {
        return ResponseUtil.success(
                "Service created successfully",
                serviceService.create(request)
        ).getBody();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ServiceResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody ServiceRequest request
    ) {
        return ResponseUtil.success(
                "Service updated successfully",
                serviceService.update(id, request)
        ).getBody();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {

        serviceService.softDelete(id);

        return ResponseUtil.<Void>success(
                "Service deleted successfully",
                null
        ).getBody();
    }
}