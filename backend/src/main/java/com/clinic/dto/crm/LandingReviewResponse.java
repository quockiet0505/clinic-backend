package com.clinic.dto.crm;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO gộp cả đánh giá phòng khám (Feedback) và đánh giá bác sĩ (DoctorReview)
 * để hiển thị trên Landing Page của Patient Web.
 */
@Data
public class LandingReviewResponse {

    // Loại đánh giá: "CLINIC" hoặc "DOCTOR"
    private String type;

    // ID gốc của bản ghi (feedbackId hoặc reviewId)
    private Integer id;

    // Thông tin người đánh giá
    private String patientName;
    private Boolean isAnonymous;

    // Số sao
    private Integer rating;

    // Nội dung bình luận
    private String comment;

    // Thời gian gửi
    private LocalDateTime createdAt;

    // Chỉ dùng cho type = "DOCTOR"
    private String doctorName;
    private String doctorExpertise;
}
