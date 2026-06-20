package com.clinic.controller.crm;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.crm.ClinicFeedbackFilterRequest;
import com.clinic.dto.crm.ClinicFeedbackReplyRequest;
import com.clinic.dto.crm.ClinicFeedbackResponse;
import com.clinic.dto.crm.ClinicFeedbackSubmitRequest;
import com.clinic.service.crm.ClinicFeedbackService;
import com.clinic.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedbacks/clinic")
@RequiredArgsConstructor
public class ClinicFeedbackController {

    private final ClinicFeedbackService clinicFeedbackService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ClinicFeedbackResponse>>> getAll(
            @ModelAttribute ClinicFeedbackFilterRequest filter
    ) {
        return ResponseUtil.success("Clinic feedbacks retrieved successfully", clinicFeedbackService.getAll(filter));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ClinicFeedbackResponse>>> getAllLegacy() {
        return ResponseUtil.success("Clinic feedbacks retrieved successfully", clinicFeedbackService.getAllLegacy());
    }

    @PostMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Void>> submitClinicFeedback(
            @Valid @RequestBody ClinicFeedbackSubmitRequest request,
            Authentication authentication
    ) {
        clinicFeedbackService.submitClinicFeedback(authentication.getName(), request);
        return ResponseUtil.success("Đánh giá phòng khám của bạn đã được gửi thành công", null);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<ClinicFeedbackResponse>>> getMyClinicFeedbacks(
            Authentication authentication
    ) {
        return ResponseUtil.success("Lấy danh sách đánh giá thành công", clinicFeedbackService.getMyClinicFeedbacks(authentication.getName()));
    }

    @PostMapping("/{id}/reply")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> replyClinicFeedback(
            @PathVariable Integer id,
            @RequestBody ClinicFeedbackReplyRequest request,
            Authentication authentication
    ) {
        clinicFeedbackService.replyClinicFeedback(id, request.getReply(), authentication.getName());
        return ResponseUtil.success("Reply sent successfully", null);
    }
}
