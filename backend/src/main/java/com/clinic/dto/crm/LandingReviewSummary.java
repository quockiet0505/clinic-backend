package com.clinic.dto.crm;

import lombok.Data;

/**
 * Thống kê tổng hợp để hiển thị điểm số trung bình trên Landing Page.
 */
@Data
public class LandingReviewSummary {
    private Double averageRating;
    private Long totalReviews;
    private java.util.List<LandingReviewResponse> reviews;
}
