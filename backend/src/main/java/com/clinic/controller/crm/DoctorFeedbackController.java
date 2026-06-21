package com.clinic.controller.crm;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.crm.DoctorFeedbackFilterRequest;
import com.clinic.dto.crm.DoctorFeedbackReplyRequest;
import com.clinic.dto.crm.DoctorFeedbackResponse;
import com.clinic.dto.crm.DoctorFeedbackSubmitRequest;
import com.clinic.service.crm.DoctorFeedbackService;
import com.clinic.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedbacks/doctor")
@RequiredArgsConstructor
public class DoctorFeedbackController {

    private final DoctorFeedbackService doctorFeedbackService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DoctorFeedbackResponse>>> getAll(
            @ModelAttribute DoctorFeedbackFilterRequest filter
    ) {
        return ResponseUtil.success("Doctor feedbacks retrieved successfully", doctorFeedbackService.getAll(filter));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<DoctorFeedbackResponse>>> getAllLegacy() {
        return ResponseUtil.success("Doctor feedbacks retrieved successfully", doctorFeedbackService.getAllLegacy());
    }

    @PostMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Void>> submitDoctorFeedback(
            @Valid @RequestBody DoctorFeedbackSubmitRequest request,
            Authentication authentication
    ) {
        doctorFeedbackService.submitDoctorFeedback(authentication.getName(), request);
        return ResponseUtil.success("Đánh giá của bạn đã được gửi thành công", null);
    }

    @PutMapping("/my/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Void>> updateDoctorFeedback(
            @PathVariable Integer id,
            @Valid @RequestBody DoctorFeedbackSubmitRequest request,
            Authentication authentication
    ) {
        doctorFeedbackService.updateDoctorFeedback(authentication.getName(), id, request);
        return ResponseUtil.success("Sửa đánh giá bác sĩ thành công", null);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<DoctorFeedbackResponse>>> getMyDoctorFeedbacks(
            Authentication authentication
    ) {
        return ResponseUtil.success("Lấy danh sách đánh giá thành công", doctorFeedbackService.getMyDoctorFeedbacks(authentication.getName()));
    }

    @PostMapping("/{id}/reply")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> replyDoctorFeedback(
            @PathVariable Integer id,
            @RequestBody DoctorFeedbackReplyRequest request,
            Authentication authentication
    ) {
        doctorFeedbackService.replyDoctorFeedback(id, request.getReply(), authentication.getName());
        return ResponseUtil.success("Reply sent successfully", null);
    }
}
