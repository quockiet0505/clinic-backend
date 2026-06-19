package com.clinic.controller.crm;

import java.util.List;

import org.springframework.http.ResponseEntity;
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

import com.clinic.common.enums.FollowUpStatus;
import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.crm.FollowUpFilterRequest;
import com.clinic.dto.crm.FollowUpRequest;
import com.clinic.dto.crm.FollowUpResponse;
import com.clinic.service.crm.FollowUpService;
import com.clinic.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<FollowUpResponse>> create(
            @Valid @RequestBody FollowUpRequest request
    ) {

        FollowUpResponse response =
                followUpService.create(request);

        return ResponseUtil.success(
                "Follow-up created successfully",
                response
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PageResponse<FollowUpResponse>>> getAll(
            @ModelAttribute FollowUpFilterRequest filter
    ) {
        return ResponseUtil.success("Follow-ups retrieved successfully", followUpService.getAll(filter));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<FollowUpResponse>>> getAllLegacy() {
        return ResponseUtil.success("Follow-ups retrieved successfully", followUpService.getAll());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<FollowUpResponse>> updateStatus(
            @PathVariable Integer id,
            @RequestParam FollowUpStatus status
    ) {

        FollowUpResponse response =
                followUpService.updateStatus(id, status);

        return ResponseUtil.success(
                "Follow-up status updated successfully",
                response
        );
    }
}