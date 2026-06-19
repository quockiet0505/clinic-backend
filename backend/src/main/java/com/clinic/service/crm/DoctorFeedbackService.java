package com.clinic.service.crm;

import com.clinic.dto.common.PageResponse;
import com.clinic.dto.crm.DoctorFeedbackFilterRequest;
import com.clinic.dto.crm.DoctorFeedbackResponse;
import com.clinic.entity.crm.DoctorReview;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.crm.DoctorFeedbackMapper;
import com.clinic.repository.crm.DoctorReviewRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.specification.crm.DoctorFeedbackSpecification;
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
public class DoctorFeedbackService {

    private final DoctorReviewRepository doctorReviewRepository;
    private final StaffRepository staffRepository;
    private final DoctorFeedbackMapper doctorFeedbackMapper;

    @Transactional(readOnly = true)
    public PageResponse<DoctorFeedbackResponse> getAll(DoctorFeedbackFilterRequest filter) {
        Specification<DoctorReview> spec = DoctorFeedbackSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<DoctorReview> page = doctorReviewRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(doctorFeedbackMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<DoctorFeedbackResponse> getAllLegacy() {
        return doctorReviewRepository.findAll().stream()
                .map(doctorFeedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void replyDoctorFeedback(Integer reviewId, String reply, Integer staffId) {
        DoctorReview review = doctorReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Doctor review not found"));
        review.setReply(reply);
        review.setRepliedAt(LocalDateTime.now());
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        review.setRepliedBy(staff);
        doctorReviewRepository.save(review);
    }
}
