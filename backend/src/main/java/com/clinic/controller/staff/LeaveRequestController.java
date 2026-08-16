package com.clinic.controller.staff;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import com.clinic.dto.staff.ReviewLeaveRequestRequest;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.staff.LeaveRequestFilterRequest;
import com.clinic.dto.staff.LeaveRequestRequest;
import com.clinic.dto.staff.LeaveRequestResponse;
import com.clinic.service.staff.LeaveRequestService;
import com.clinic.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'NURSE', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PageResponse<LeaveRequestResponse>>> getAll(
            @ModelAttribute LeaveRequestFilterRequest filter,
            Authentication authentication
    ) {
        return ResponseUtil.success("Leave requests retrieved successfully", leaveRequestService.getAll(filter, authentication));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'NURSE', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getAllLegacy() {
        return ResponseUtil.success("Leave requests retrieved successfully", leaveRequestService.getAllLegacy());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'NURSE', 'DOCTOR')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> create(
            @Valid @RequestBody LeaveRequestRequest request,
            Authentication authentication
    ) {
        return ResponseUtil.success("Leave request created", leaveRequestService.create(request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'NURSE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        leaveRequestService.delete(id);
        return ResponseUtil.success("Leave request deleted", null);
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'NURSE')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> review(
            @PathVariable Integer id,
            @Valid @RequestBody ReviewLeaveRequestRequest request,
            Authentication authentication
    ) {
        return ResponseUtil.success(
                "Leave request reviewed successfully",
                leaveRequestService.reviewLeaveRequest(id, request.getStatus(), authentication.getName(), request.getRejectionReason())
        );
    }
}
