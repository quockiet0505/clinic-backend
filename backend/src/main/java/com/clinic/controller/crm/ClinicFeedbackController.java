package com.clinic.controller.crm;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.crm.ClinicFeedbackFilterRequest;
import com.clinic.dto.crm.ClinicFeedbackReplyRequest;
import com.clinic.dto.crm.ClinicFeedbackResponse;
import com.clinic.service.crm.ClinicFeedbackService;
import com.clinic.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{id}/reply")
    public ResponseEntity<ApiResponse<Void>> replyClinicFeedback(
            @PathVariable Integer id,
            @RequestBody ClinicFeedbackReplyRequest request,
            Authentication authentication
    ) {
        Integer staffId = getStaffIdFromAuth(authentication);
        clinicFeedbackService.replyClinicFeedback(id, request.getReply(), staffId);
        return ResponseUtil.success("Reply sent successfully", null);
    }

    private Integer getStaffIdFromAuth(Authentication authentication) {
        return 1;
    }
}
