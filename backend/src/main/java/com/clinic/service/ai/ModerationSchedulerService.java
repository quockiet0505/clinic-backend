package com.clinic.service.ai;

import com.clinic.repository.crm.DoctorReviewRepository;
import com.clinic.repository.crm.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dịch vụ quét định kỳ các bình luận bị kẹt ở trạng thái PENDING.
 *
 * === LÝ DO CẦN SCHEDULED TASK NÀY ===
 * Khi bệnh nhân gửi đánh giá, Spring Boot gọi AI Server ngay lập tức (@Async).
 * Nhưng nếu AI Server đang tắt/restart lúc đó:
 *   - Bình luận được lưu với aiStatus = 'PENDING'
 *   - AiModerationService gọi AI thất bại → fallback auto-approve
 *   → Vậy thực ra không có vấn đề với PENDING bị kẹt vì luôn có fallback.
 *
 * Tuy nhiên, scheduled task này vẫn hữu ích để:
 *   1. Xử lý lại các bình luận được auto-approve do AI Server lỗi
 *      (nếu admin muốn gắn cờ PENDING để AI duyệt lại sau khi server ổn định)
 *   2. Logging/monitoring số lượng PENDING để phát hiện sự cố sớm
 *   3. Cung cấp cơ chế thử lại (retry) trong trường hợp muốn re-moderate
 *
 * === THỜI GIAN CHẠY ===
 * Mỗi 30 phút một lần (fixedDelay = 30 * 60 * 1000 ms).
 * Chỉ quét các bình luận PENDING tồn tại trên 5 phút (tránh đụng bình luận mới vừa submit).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationSchedulerService {

    private final FeedbackRepository feedbackRepository;
    private final DoctorReviewRepository doctorReviewRepository;
    private final AiModerationService aiModerationService;

    /**
     * Chạy mỗi 30 phút, quét lại các đánh giá phòng khám PENDING > 5 phút.
     * Cron expression chạy định kỳ 30 phút một lần.
     */
    @Scheduled(cron = "0 */30 * * * *")
    @Transactional(readOnly = true)
    public void retryPendingClinicFeedbacks() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        List<com.clinic.entity.crm.Feedback> pendingList =
            feedbackRepository.findPendingOlderThan(cutoff);

        if (pendingList.isEmpty()) {
            log.debug("[Scheduler] Không có Feedback PENDING nào cần xử lý lại.");
            return;
        }

        log.info("[Scheduler] Bắt đầu re-moderate {} Feedback PENDING...", pendingList.size());
        for (var feedback : pendingList) {
            try {
                aiModerationService.moderateFeedbackAsync(feedback.getFeedbackId());
                log.info("[Scheduler] Đã kích hoạt kiểm duyệt lại Feedback #{}", feedback.getFeedbackId());
            } catch (Exception e) {
                log.error("[Scheduler] Lỗi khi re-moderate Feedback #{}: {}", feedback.getFeedbackId(), e.getMessage());
            }
        }
        log.info("[Scheduler] Hoàn thành re-moderate {} Feedback.", pendingList.size());
    }

    /**
     * Chạy mỗi 30 phút, quét lại các đánh giá bác sĩ PENDING > 5 phút.
     */
    @Scheduled(cron = "0 */30 * * * *")
    @Transactional(readOnly = true)
    public void retryPendingDoctorReviews() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        List<com.clinic.entity.crm.DoctorReview> pendingList =
            doctorReviewRepository.findPendingOlderThan(cutoff);

        if (pendingList.isEmpty()) {
            log.debug("[Scheduler] Không có DoctorReview PENDING nào cần xử lý lại.");
            return;
        }

        log.info("[Scheduler] Bắt đầu re-moderate {} DoctorReview PENDING...", pendingList.size());
        for (var review : pendingList) {
            try {
                aiModerationService.moderateDoctorReviewAsync(review.getReviewId());
                log.info("[Scheduler] Đã kích hoạt kiểm duyệt lại DoctorReview #{}", review.getReviewId());
            } catch (Exception e) {
                log.error("[Scheduler] Lỗi khi re-moderate DoctorReview #{}: {}", review.getReviewId(), e.getMessage());
            }
        }
        log.info("[Scheduler] Hoàn thành re-moderate {} DoctorReview.", pendingList.size());
    }

    /**
     * Thống kê định kỳ mỗi giờ — Log ra tổng số bình luận theo từng trạng thái.
     * Giúp admin phát hiện sớm nếu AI Server có vấn đề (quá nhiều PENDING).
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional(readOnly = true)
    public void logModerationStats() {
        long feedbackPending = feedbackRepository.countByAiStatus("PENDING");
        long feedbackApproved = feedbackRepository.countByAiStatus("APPROVED");
        long feedbackRejected = feedbackRepository.countByAiStatus("REJECTED");

        long reviewPending = doctorReviewRepository.countByAiStatus("PENDING");
        long reviewApproved = doctorReviewRepository.countByAiStatus("APPROVED");
        long reviewRejected = doctorReviewRepository.countByAiStatus("REJECTED");

        log.info("""
            ╔═══════════════════════════════════════════╗
            ║      AI MODERATION STATS (mỗi giờ)        ║
            ╠═══════════════════════════════════════════╣
            ║ Đánh giá phòng khám (Feedback):            ║
            ║   PENDING  : {}                            ║
            ║   APPROVED : {}                            ║
            ║   REJECTED : {}                            ║
            ╠═══════════════════════════════════════════╣
            ║ Đánh giá bác sĩ (DoctorReview):           ║
            ║   PENDING  : {}                            ║
            ║   APPROVED : {}                            ║
            ║   REJECTED : {}                            ║
            ╚═══════════════════════════════════════════╝
            """,
            feedbackPending, feedbackApproved, feedbackRejected,
            reviewPending, reviewApproved, reviewRejected
        );

        if (feedbackPending + reviewPending > 50) {
            log.warn("[Scheduler] CẢNH BÁO: Có {} bình luận PENDING! Kiểm tra AI Server.", feedbackPending + reviewPending);
        }
    }
}
