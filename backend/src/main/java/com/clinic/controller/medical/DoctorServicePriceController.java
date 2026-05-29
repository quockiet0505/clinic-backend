package com.clinic.controller.medical;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.medical.DoctorServicePriceRequest;
import com.clinic.dto.medical.DoctorServicePriceResponse;
import com.clinic.service.medical.DoctorServicePriceService;
import com.clinic.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/doctor-prices")
@RequiredArgsConstructor
public class DoctorServicePriceController {

    private final DoctorServicePriceService priceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ApiResponse<List<DoctorServicePriceResponse>> getAll() {
        return ResponseUtil.success(
                "Doctor prices fetched successfully",
                priceService.getAll()
        ).getBody();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DoctorServicePriceResponse> createOrUpdate(
            @Valid @RequestBody DoctorServicePriceRequest request
    ) {
        return ResponseUtil.success(
                "Doctor price saved successfully",
                priceService.createOrUpdate(request)
        ).getBody();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {

        priceService.delete(id);

        return ResponseUtil.<Void>success(
            "Service deleted successfully",
            null
        ).getBody();
    }
}