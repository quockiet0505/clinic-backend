
package com.clinic.controller.staff;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.common.enums.StaffType;
import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.staff.StaffRequest;
import com.clinic.dto.staff.StaffResponse;
import com.clinic.service.staff.StaffService;
import com.clinic.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/staffs")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

        @GetMapping
        public ApiResponse<List<StaffResponse>> getAll(
                @RequestParam(required = false) Integer expertiseId,
                @RequestParam(required = false) StaffType staffType
        ) {
        return ResponseUtil.success(
                "Staffs fetched successfully",
                staffService.getAll(expertiseId, staffType)
        ).getBody();
        }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StaffResponse> create(
            @Valid @RequestBody StaffRequest request
    ) {
        return ResponseUtil.success(
                "Staff created successfully",
                staffService.create(request)
        ).getBody();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StaffResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody StaffRequest request
    ) {
        return ResponseUtil.success(
                "Staff updated successfully",
                staffService.update(id, request)
        ).getBody();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {

        staffService.softDelete(id);

        return ResponseUtil.<Void>success(
                "Staff deleted successfully",
                null
        ).getBody();
    }

    @GetMapping("/doctors")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<StaffResponse>> getAllDoctors() {
        return ResponseUtil.success(
                "Doctors fetched successfully",
                staffService.getAllDoctors()
        ).getBody();
    }

    @GetMapping("/doctors/featured")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<StaffResponse>> getFeaturedDoctors() {
        return ResponseUtil.success(
                "Featured doctors fetched successfully",
                staffService.getFeaturedDoctors()
        ).getBody();
    }

    @GetMapping("/{id}")
        public ApiResponse<StaffResponse> getById(
                @PathVariable Integer id
        ) {
        return ResponseUtil.success(
                "Staff fetched successfully",
                staffService.getById(id)
        ).getBody();
        }
}

