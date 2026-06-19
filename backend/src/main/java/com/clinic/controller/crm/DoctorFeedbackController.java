package com.clinic.controller.crm;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.crm.DoctorFeedbackFilterRequest;
import com.clinic.dto.crm.DoctorFeedbackReplyRequest;
import com.clinic.dto.crm.DoctorFeedbackResponse;
import com.clinic.service.crm.DoctorFeedbackService;
import com.clinic.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{id}/reply")
    public ResponseEntity<ApiResponse<Void>> replyDoctorFeedback(
            @PathVariable Integer id,
            @RequestBody DoctorFeedbackReplyRequest request,
            Authentication authentication
    ) {
        Integer staffId = getStaffIdFromAuth(authentication);
        doctorFeedbackService.replyDoctorFeedback(id, request.getReply(), staffId);
        return ResponseUtil.success("Reply sent successfully", null);
    }

    private Integer getStaffIdFromAuth(Authentication authentication) {
        return 1;
    }
}
