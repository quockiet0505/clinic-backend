package com.clinic.controller.crm;

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

import com.clinic.common.enums.FollowUpStatus;
import com.clinic.dto.crm.FollowUpRequest;
import com.clinic.dto.crm.FollowUpResponse;
import com.clinic.service.crm.FollowUpService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    /**
     * Create a new follow-up schedule for a patient.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<FollowUpResponse> create(@Valid @RequestBody FollowUpRequest request) {
        return ResponseEntity.ok(followUpService.create(request));
    }

    /**
     * Get all follow-up records.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<List<FollowUpResponse>> getAll() {
        return ResponseEntity.ok(followUpService.getAll());
    }

    /**
     * Update the status of a follow-up (e.g., CONFIRMED, COMPLETED).
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<FollowUpResponse> updateStatus(
            @PathVariable Integer id,
            @RequestParam FollowUpStatus status) {
        return ResponseEntity.ok(followUpService.updateStatus(id, status));
    }
}