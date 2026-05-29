
package com.clinic.controller.patient;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.patient.PatientRequest;
import com.clinic.dto.patient.PatientResponse;
import com.clinic.service.patient.PatientService;
import com.clinic.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ApiResponse<List<PatientResponse>> getAll() {
        return ResponseUtil.success(
                "Patients fetched successfully",
                patientService.getAllActive()
        ).getBody();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public ApiResponse<PatientResponse> getById(@PathVariable Integer id) {
        return ResponseUtil.success(
                "Patient fetched successfully",
                patientService.getById(id)
        ).getBody();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<PatientResponse> create(
            @Valid @RequestBody PatientRequest request
    ) {
        return ResponseUtil.success(
                "Patient created successfully",
                patientService.create(request)
        ).getBody();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'PATIENT')")
    public ApiResponse<PatientResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody PatientRequest request
    ) {
        return ResponseUtil.success(
                "Patient updated successfully",
                patientService.update(id, request)
        ).getBody();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {

        patientService.softDelete(id);

        return ResponseUtil.<Void>success(
                "Patient deleted successfully",
                null
        ).getBody();
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<PatientResponse> getMyProfile(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseUtil.success(
                "Profile fetched successfully",
                patientService.getProfileByEmail(email)
        ).getBody();
    }
}

