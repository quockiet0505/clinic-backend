package com.clinic.service.ai;

import com.clinic.repository.base.SystemSettingRepository;
import com.clinic.repository.crm.DoctorReviewRepository;
import com.clinic.repository.crm.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Dịch vụ kiểm duyệt bình luận tự động bằng AI.
 *
 * Cơ chế:
 *  - Khi bệnh nhân gửi/sửa đánh giá, Backend lưu vào DB với aiStatus = "PENDING"
 *    rồi trả về HTTP 200 ngay cho người dùng (không chặn).
 *  - Các phương thức @Async trong class này được chạy nền trên thread pool riêng.
 *  - Sau khi AI Server trả kết quả, service cập nhật aiStatus = "APPROVED" / "REJECTED".
 *  - Trang chủ Patient Web chỉ query những bản ghi có aiStatus = "APPROVED".
 *  - Đánh giá đã APPROVED sẽ không bao giờ bị kiểm duyệt lại (trừ khi bệnh nhân sửa comment).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiModerationService {

    private final FeedbackRepository feedbackRepository;
    private final DoctorReviewRepository doctorReviewRepository;
    private final RestTemplate restTemplate;
    private final SystemSettingRepository systemSettingRepository;

    @Value("${application.ai-server-url}")
    private String aiServerUrl;

    @Value("${application.ai-moderation-url}")
    private String aiModerationUrl;

    /**
     * Kiểm duyệt đánh giá phòng khám bất đồng bộ.
     * Chỉ gọi khi bình luận mới được tạo hoặc nội dung comment bị thay đổi.
     */
    @Async("aiModerationExecutor")
    public void moderateFeedbackAsync(Integer feedbackId) {
        feedbackRepository.findById(feedbackId).ifPresent(feedback -> {
            try {
                boolean aiEnabled = systemSettingRepository.findById("AI_MODERATION_ENABLED")
                        .map(setting -> "true".equalsIgnoreCase(setting.getSettingValue()))
                        .orElse(false);

                if (!aiEnabled) {
                    log.info("[AI Moderation] Feedback #{} - AI Moderation disabled, leaving as PENDING for manual review", feedbackId);
                    return;
                }

                ModerationResult result = callModerationApi(feedback.getComment(), feedback.getRating());
                feedback.setAiStatus(result.approved() ? "APPROVED" : "REJECTED");
                feedback.setAiModerationNote(result.reason());
                feedbackRepository.save(feedback);
                log.info("[AI Moderation] Feedback #{} => {} | Reason: {}", feedbackId, feedback.getAiStatus(), result.reason());
            } catch (Exception e) {
                // Nếu AI Server lỗi (tắt máy, timeout), giữ nguyên PENDING và ghi chú lỗi để Admin duyệt bằng cơm
                feedback.setAiModerationNote("AI Server error: " + e.getMessage());
                feedbackRepository.save(feedback);
                log.warn("[AI Moderation] Feedback #{} - AI Server error, left as PENDING: {}", feedbackId, e.getMessage());
            }
        });
    }

    /**
     * Kiểm duyệt đánh giá bác sĩ bất đồng bộ.
     * Chỉ gọi khi bình luận mới được tạo hoặc nội dung comment bị thay đổi.
     */
    @Async("aiModerationExecutor")
    public void moderateDoctorReviewAsync(Integer reviewId) {
        doctorReviewRepository.findById(reviewId).ifPresent(review -> {
            try {
                boolean aiEnabled = systemSettingRepository.findById("AI_MODERATION_ENABLED")
                        .map(setting -> "true".equalsIgnoreCase(setting.getSettingValue()))
                        .orElse(false);

                if (!aiEnabled) {
                    log.info("[AI Moderation] DoctorReview #{} - AI Moderation disabled, leaving as PENDING for manual review", reviewId);
                    return;
                }

                ModerationResult result = callModerationApi(review.getComment(), review.getRating());
                review.setAiStatus(result.approved() ? "APPROVED" : "REJECTED");
                review.setAiModerationNote(result.reason());
                doctorReviewRepository.save(review);
                log.info("[AI Moderation] DoctorReview #{} => {} | Reason: {}", reviewId, review.getAiStatus(), result.reason());
            } catch (Exception e) {
                // Nếu AI Server lỗi, giữ nguyên PENDING và ghi chú lỗi để Admin duyệt bằng cơm
                review.setAiModerationNote("AI Server error: " + e.getMessage());
                doctorReviewRepository.save(review);
                log.warn("[AI Moderation] DoctorReview #{} - AI Server error, left as PENDING: {}", reviewId, e.getMessage());
            }
        });
    }

    /**
     * Gọi API kiểm duyệt của FastAPI AI Server.
     * Endpoint: POST /api/v1/moderation/check
     */
    private ModerationResult callModerationApi(String comment, Integer rating) {
        String url = aiModerationUrl + "/api/v1/moderation/check";

        Map<String, Object> body = new HashMap<>();
        body.put("comment", comment != null ? comment : "");
        body.put("rating", rating != null ? rating : 0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        if (response == null) {
            throw new RuntimeException("Empty response from AI Server");
        }

        boolean approved = Boolean.TRUE.equals(response.get("approved"));
        String reason = (String) response.getOrDefault("reason", "");
        return new ModerationResult(approved, reason);
    }

    private record ModerationResult(boolean approved, String reason) {}
}
