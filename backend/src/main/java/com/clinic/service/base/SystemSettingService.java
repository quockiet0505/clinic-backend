package com.clinic.service.base;

import com.clinic.entity.base.SystemSetting;
import com.clinic.repository.base.SystemSettingRepository;
import com.clinic.service.ai.AiModerationService;
import com.clinic.repository.crm.FeedbackRepository;
import com.clinic.repository.crm.DoctorReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemSettingService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Autowired
    private AiModerationService aiModerationService;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private DoctorReviewRepository doctorReviewRepository;

    public Map<String, String> getAllSettingsAsMap() {
        List<SystemSetting> settings = systemSettingRepository.findAll();
        Map<String, String> map = new HashMap<>();
        for (SystemSetting setting : settings) {
            map.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return map;
    }

    public void updateSettings(Map<String, String> settingsMap) {
        boolean aiTurnedOn = false;

        for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
            SystemSetting setting = systemSettingRepository.findById(entry.getKey()).orElse(new SystemSetting());
            setting.setSettingKey(entry.getKey());
            setting.setSettingValue(entry.getValue());
            systemSettingRepository.save(setting);

            if ("AI_MODERATION_ENABLED".equals(entry.getKey()) && "true".equalsIgnoreCase(entry.getValue())) {
                aiTurnedOn = true;
            }
        }

        if (aiTurnedOn) {
            triggerPendingModeration();
        }
    }

    private void triggerPendingModeration() {
        List<com.clinic.entity.crm.Feedback> pendingFeedbacks = feedbackRepository.findByAiStatus("PENDING");
        for (com.clinic.entity.crm.Feedback fb : pendingFeedbacks) {
            aiModerationService.moderateFeedbackAsync(fb.getFeedbackId());
        }

        List<com.clinic.entity.crm.DoctorReview> pendingReviews = doctorReviewRepository.findByAiStatus("PENDING");
        for (com.clinic.entity.crm.DoctorReview dr : pendingReviews) {
            aiModerationService.moderateDoctorReviewAsync(dr.getReviewId());
        }
    }
}
