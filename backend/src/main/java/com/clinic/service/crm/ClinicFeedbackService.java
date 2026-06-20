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
import com.clinic.dto.crm.ClinicFeedbackSubmitRequest;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.patient.Patient;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.patient.PatientRepository;
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
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;

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
    public void submitClinicFeedback(String email, ClinicFeedbackSubmitRequest request) {
        if (feedbackRepository.existsByMedicalRecord_RecordId(request.getRecordId())) {
            throw new RuntimeException("Bạn đã gửi đánh giá phòng khám cho hồ sơ này rồi.");
        }

        Patient patient = patientRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        MedicalRecord record = medicalRecordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RuntimeException("Medical record not found"));

        if (!record.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new RuntimeException("Không có quyền đánh giá hồ sơ này.");
        }

        Feedback feedback = new Feedback();
        feedback.setMedicalRecord(record);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        feedback.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);
        feedback.setCreatedAt(LocalDateTime.now());

        feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public List<ClinicFeedbackResponse> getMyClinicFeedbacks(String email) {
        return feedbackRepository.findByMedicalRecord_Patient_Account_EmailOrderByCreatedAtDesc(email).stream()
                .map(clinicFeedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void replyClinicFeedback(Integer feedbackId, String reply, String email) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        feedback.setReply(reply);
        feedback.setRepliedAt(LocalDateTime.now());
        Staff staff = staffRepository.findAll().stream()
                .filter(s -> s.getAccount() != null && s.getAccount().getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        feedback.setRepliedBy(staff);
        feedbackRepository.save(feedback);
    }
}
