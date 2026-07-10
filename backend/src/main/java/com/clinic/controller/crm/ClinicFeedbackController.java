package com.clinic.controller.crm;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.crm.ClinicFeedbackFilterRequest;
import com.clinic.dto.crm.ClinicFeedbackReplyRequest;
import com.clinic.dto.crm.ClinicFeedbackResponse;
import com.clinic.dto.crm.ClinicFeedbackSubmitRequest;
import com.clinic.dto.crm.LandingReviewResponse;
import com.clinic.dto.crm.LandingReviewSummary;
import com.clinic.repository.crm.DoctorReviewRepository;
import com.clinic.repository.crm.FeedbackRepository;
import com.clinic.service.crm.ClinicFeedbackService;
import com.clinic.service.crm.DoctorFeedbackService;
import com.clinic.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/feedbacks/clinic")
@RequiredArgsConstructor
public class ClinicFeedbackController {

    private final ClinicFeedbackService clinicFeedbackService;
    private final DoctorFeedbackService doctorFeedbackService;
    private final FeedbackRepository feedbackRepository;
    private final DoctorReviewRepository doctorReviewRepository;

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

    @PutMapping("/my/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Void>> updateClinicFeedback(
            @PathVariable Integer id,
            @Valid @RequestBody ClinicFeedbackSubmitRequest request,
            Authentication authentication
    ) {
        clinicFeedbackService.updateClinicFeedback(authentication.getName(), id, request);
        return ResponseUtil.success("Sửa đánh giá phòng khám thành công", null);
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

    @PutMapping("/{id}/ai-status")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateAiStatus(
            @PathVariable Integer id,
            @RequestParam String status,
            Authentication authentication
    ) {
        clinicFeedbackService.updateAiStatus(id, status, authentication.getName());
        return ResponseUtil.success("AI Moderation status updated successfully", null);
    }


    /**
     * API công khai cho Patient Web Landing Page.
     * Trả về điểm trung bình tổng hợp + danh sách đánh giá tích cực của cả
     * phòng khám (Feedback) và bác sĩ (DoctorReview) đã được AI phê duyệt.
     */
    @GetMapping("/landing")
    public ResponseEntity<ApiResponse<LandingReviewSummary>> getLandingReviews() {
        // Lấy tối đa 30 đánh giá mỗi loại (tổng tối đa 60 cho việc mở rộng nhiều giai đoạn)
        List<LandingReviewResponse> clinicReviews = clinicFeedbackService.getLandingClinicReviews(30);
        List<LandingReviewResponse> doctorReviews = doctorFeedbackService.getLandingDoctorReviews(30);


        // Gộp và sắp xếp theo ngày mới nhất
        List<LandingReviewResponse> combined = new ArrayList<>();
        combined.addAll(clinicReviews);
        combined.addAll(doctorReviews);
        combined.sort(Comparator.comparing(LandingReviewResponse::getCreatedAt).reversed());

        // Tính điểm trung bình tổng hợp
        Double clinicAvg = feedbackRepository.getAverageApprovedRating();
        Long clinicCount = feedbackRepository.countByAiStatus("APPROVED");
        Double doctorAvg = doctorReviewRepository.getAverageApprovedRating();
        Long doctorCount = doctorReviewRepository.countByAiStatus("APPROVED");


        long totalReviews = (clinicCount != null ? clinicCount : 0) + (doctorCount != null ? doctorCount : 0);
        double avgRating = 0.0;
        if (totalReviews > 0) {
            avgRating = ((clinicAvg != null ? clinicAvg : 0.0) * (clinicCount != null ? clinicCount : 0)
                    + (doctorAvg != null ? doctorAvg : 0.0) * (doctorCount != null ? doctorCount : 0))
                    / totalReviews;
        }

        LandingReviewSummary summary = new LandingReviewSummary();
        summary.setAverageRating(Math.round(avgRating * 10.0) / 10.0);
        summary.setTotalReviews(totalReviews);
        summary.setReviews(combined);

        return ResponseUtil.success("Landing reviews retrieved successfully", summary);
    }
}
