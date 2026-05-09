package com.clinic.controller.crm;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.crm.FeedbackRequest;
import com.clinic.service.crm.FeedbackService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService feedbackService;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')") // Only patients leave feedback
    public ResponseEntity<?> create(@Valid @RequestBody FeedbackRequest request) {
        feedbackService.create(request);
        return ResponseEntity.ok("Feedback submitted successfully");
    }
}