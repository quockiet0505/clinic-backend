package com.clinic.controller.staff;

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

import com.clinic.common.enums.LeaveStatus;
import com.clinic.dto.staff.LeaveRequestRequest;
import com.clinic.dto.staff.LeaveRequestResponse;
import com.clinic.service.staff.LeaveRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    /**
     * Get all leave requests for administration view.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<LeaveRequestResponse>> getAll() {
        return ResponseEntity.ok(leaveRequestService.getAll());
    }

    /**
     * Create a new leave request for the authenticated staff member.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'LAB_TECH')")
    public ResponseEntity<LeaveRequestResponse> create(@Valid @RequestBody LeaveRequestRequest request) {
        return ResponseEntity.ok(leaveRequestService.create(request));
    }

    /**
     * Approve or reject a leave request.
     * Required parameters: status (APPROVED/REJECTED), approverId, and rejectionReason if rejected.
     */
    @PatchMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<LeaveRequestResponse> reviewLeaveRequest(
            @PathVariable Integer id,
            @RequestParam LeaveStatus status,
            @RequestParam Integer approverId,
            @RequestParam(required = false) String rejectionReason) {
        
        return ResponseEntity.ok(leaveRequestService.reviewLeaveRequest(id, status, approverId, rejectionReason));
    }
}