
package com.clinic.controller.prescription;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.clinic.dto.prescription.MedicineFilterRequest;
import com.clinic.dto.prescription.MedicineRequest;
import com.clinic.dto.prescription.MedicineResponse;
import com.clinic.service.prescription.MedicineService;
import com.clinic.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'NURSE', 'DOCTOR')")
    public ApiResponse<PageResponse<MedicineResponse>> getAll(
            @ModelAttribute MedicineFilterRequest filter
    ) {
        return ResponseUtil.success(
                "Medicines fetched successfully",
                medicineService.getAll(filter)
        ).getBody();
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'NURSE', 'DOCTOR')")
    public ApiResponse<List<MedicineResponse>> getAllLegacy() {
        return ResponseUtil.success(
                "Medicines fetched successfully",
                medicineService.getAllActive()
        ).getBody();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'NURSE')")
    public ApiResponse<MedicineResponse> create(
            @Valid @RequestBody MedicineRequest request
    ) {
        return ResponseUtil.success(
                "Medicine created successfully",
                medicineService.create(request)
        ).getBody();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'NURSE')")
    public ApiResponse<MedicineResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody MedicineRequest request
    ) {
        return ResponseUtil.success(
                "Medicine updated successfully",
                medicineService.update(id, request)
        ).getBody();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {

        medicineService.softDelete(id);

        return ResponseUtil.<Void>success(
                "Medicine deleted successfully",
                null
        ).getBody();
    }
}

