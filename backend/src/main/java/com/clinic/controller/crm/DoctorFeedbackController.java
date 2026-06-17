package com.clinic.controller.crm;

import com.clinic.dto.common.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<DoctorFeedbackResponse>>> getDoctorFeedbacks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        List<DoctorFeedbackResponse> data = doctorFeedbackService.getDoctorFeedbacks(search, rating, fromDate, toDate);
        return ResponseUtil.success("Success", data);
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