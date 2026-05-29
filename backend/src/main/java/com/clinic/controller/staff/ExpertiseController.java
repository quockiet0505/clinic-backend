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
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.staff.ExpertiseRequest;
import com.clinic.dto.staff.ExpertiseResponse;
import com.clinic.service.staff.ExpertiseService;
import com.clinic.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/expertise")
@RequiredArgsConstructor
public class ExpertiseController {

    private final ExpertiseService expertiseService;

    @GetMapping
    public ApiResponse<List<ExpertiseResponse>> getAll() {

        return ResponseUtil.success(
                "Expertise list fetched successfully",
                expertiseService.getAll()
        ).getBody();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ExpertiseResponse> create(
            @Valid @RequestBody ExpertiseRequest request
    ) {

        return ResponseUtil.success(
                "Expertise created successfully",
                expertiseService.create(request)
        ).getBody();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ExpertiseResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody ExpertiseRequest request
    ) {

        return ResponseUtil.success(
                "Expertise updated successfully",
                expertiseService.update(id, request)
        ).getBody();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {

        expertiseService.delete(id);

        return ResponseUtil.<Void>success(
                "Expertise deleted successfully",
                null
        ).getBody();
    }
}