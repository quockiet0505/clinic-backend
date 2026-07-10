package com.clinic.service.crm;

import com.clinic.dto.common.PageResponse;
import com.clinic.dto.crm.ClinicFeedbackFilterRequest;
import com.clinic.dto.crm.ClinicFeedbackResponse;
import com.clinic.dto.crm.LandingReviewResponse;
import com.clinic.entity.crm.Feedback;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.crm.ClinicFeedbackMapper;
import com.clinic.repository.crm.FeedbackRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.service.ai.AiModerationService;
import com.clinic.specification.crm.ClinicFeedbackSpecification;
import com.clinic.dto.crm.ClinicFeedbackSubmitRequest;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.patient.Patient;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.util.FilterUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final AiModerationService aiModerationService;

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
        MedicalRecord record;
        if (request.getRecordId() != null) {
            record = medicalRecordRepository.findById(request.getRecordId())
                    .orElseThrow(() -> new RuntimeException("Medical record not found"));
        } else if (request.getAppointmentId() != null) {
            record = medicalRecordRepository.findByAppointment_AppointmentId(request.getAppointmentId())
                    .orElseThrow(() -> new RuntimeException("Medical record not found for this appointment"));
        } else {
            throw new RuntimeException("Either Record ID or Appointment ID is required");
        }

        if (feedbackRepository.existsByMedicalRecord_RecordId(record.getRecordId())) {
            throw new RuntimeException("Bạn đã gửi đánh giá phòng khám cho hồ sơ này rồi.");
        }

        Patient patient = patientRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!record.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new RuntimeException("Không có quyền đánh giá hồ sơ này.");
        }

        Feedback feedback = new Feedback();
        feedback.setMedicalRecord(record);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        feedback.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setAiStatus("PENDING");

        Feedback saved = feedbackRepository.save(feedback);
        // Kích hoạt kiểm duyệt AI bất đồng bộ (không block HTTP response)
        aiModerationService.moderateFeedbackAsync(saved.getFeedbackId());
    }

    @Transactional
    public void updateClinicFeedback(String email, Integer id, ClinicFeedbackSubmitRequest request) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đánh giá không tồn tại"));

        if (feedback.getMedicalRecord() == null || feedback.getMedicalRecord().getPatient() == null || 
            feedback.getMedicalRecord().getPatient().getAccount() == null || 
            !feedback.getMedicalRecord().getPatient().getAccount().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền sửa đánh giá này");
        }

        long hours = java.time.temporal.ChronoUnit.HOURS.between(feedback.getCreatedAt(), LocalDateTime.now());
        if (hours >= 24) {
            throw new RuntimeException("Chỉ có thể sửa đánh giá trong vòng 24 giờ sau khi gửi");
        }

        boolean commentChanged = !java.util.Objects.equals(feedback.getComment(), request.getComment());

        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        feedback.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);

        // Chỉ reset PENDING và chạy lại AI nếu nội dung bình luận thay đổi
        if (commentChanged) {
            feedback.setAiStatus("PENDING");
        }

        feedbackRepository.save(feedback);

        if (commentChanged) {
            aiModerationService.moderateFeedbackAsync(feedback.getFeedbackId());
        }
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

    @Transactional
    public void updateAiStatus(Integer feedbackId, String status, String email) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        feedback.setAiStatus(status);
        feedback.setAiModerationNote("Cập nhật thủ công bởi: " + email);
        feedbackRepository.save(feedback);
    }



    /**
     * Lấy danh sách đánh giá phòng khám để hiển thị trên Landing Page.
     * Chỉ trả về các đánh giá đã được AI phê duyệt (aiStatus = APPROVED) và rating >= 4.
     * Tối đa 6 bản ghi mới nhất (dùng cho Grid hiển thị mặc định).
     */
    @Transactional(readOnly = true)
    public List<LandingReviewResponse> getLandingClinicReviews(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return feedbackRepository.findLandingFeedbacks(4, pageable).stream()
                .map(f -> {
                    LandingReviewResponse dto = new LandingReviewResponse();
                    dto.setType("CLINIC");
                    dto.setId(f.getFeedbackId());
                    dto.setRating(f.getRating());
                    dto.setComment(f.getComment());
                    dto.setCreatedAt(f.getCreatedAt());
                    dto.setIsAnonymous(f.getIsAnonymous());
                    if (Boolean.TRUE.equals(f.getIsAnonymous())) {
                        dto.setPatientName("Bệnh nhân ẩn danh");
                    } else if (f.getMedicalRecord() != null && f.getMedicalRecord().getPatient() != null) {
                        dto.setPatientName(f.getMedicalRecord().getPatient().getFullName());
                    } else {
                        dto.setPatientName("Bệnh nhân");
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
