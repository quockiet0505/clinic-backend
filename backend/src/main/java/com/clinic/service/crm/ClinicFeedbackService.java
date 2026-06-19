package com.clinic.service.crm;

import com.clinic.dto.common.PageResponse;
import com.clinic.dto.crm.ClinicFeedbackFilterRequest;
import com.clinic.dto.crm.ClinicFeedbackResponse;
import com.clinic.entity.crm.Feedback;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.crm.ClinicFeedbackMapper;
import com.clinic.repository.crm.FeedbackRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.specification.crm.ClinicFeedbackSpecification;
import com.clinic.util.FilterUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClinicFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final StaffRepository staffRepository;
    private final ClinicFeedbackMapper clinicFeedbackMapper;

    @Transactional(readOnly = true)
    public PageResponse<ClinicFeedbackResponse> getAll(ClinicFeedbackFilterRequest filter) {
        Specification<Feedback> spec = ClinicFeedbackSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<Feedback> page = feedbackRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(clinicFeedbackMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<ClinicFeedbackResponse> getAllLegacy() {
        return feedbackRepository.findAll().stream()
                .map(clinicFeedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void replyClinicFeedback(Integer feedbackId, String reply, Integer staffId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        feedback.setReply(reply);
        feedback.setRepliedAt(LocalDateTime.now());
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        feedback.setRepliedBy(staff);
        feedbackRepository.save(feedback);
    }
}
