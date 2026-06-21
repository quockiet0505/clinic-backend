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
import com.clinic.dto.crm.DoctorFeedbackSubmitRequest;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.patient.Patient;
import com.clinic.repository.appointment.AppointmentRepository;
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
public class DoctorFeedbackService {

    private final DoctorReviewRepository doctorReviewRepository;
    private final StaffRepository staffRepository;
    private final DoctorFeedbackMapper doctorFeedbackMapper;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

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
    public void submitDoctorFeedback(String email, DoctorFeedbackSubmitRequest request) {
        if (doctorReviewRepository.existsByAppointment_AppointmentId(request.getAppointmentId())) {
            throw new RuntimeException("Bạn đã gửi đánh giá cho lịch khám này rồi.");
        }

        Patient patient = patientRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new RuntimeException("Không có quyền đánh giá lịch khám này.");
        }

        if (!"COMPLETED".equals(appointment.getStatus().name())) {
            throw new RuntimeException("Chỉ có thể đánh giá sau khi hoàn thành khám.");
        }

        Staff doctor = staffRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        DoctorReview review = new DoctorReview();
        review.setDoctor(doctor);
        review.setPatient(patient);
        review.setAppointment(appointment);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);
        review.setCreatedAt(LocalDateTime.now());

        doctorReviewRepository.save(review);
    }

    @Transactional
    public void updateDoctorFeedback(String email, Integer id, DoctorFeedbackSubmitRequest request) {
        DoctorReview review = doctorReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đánh giá không tồn tại"));

        if (review.getPatient() == null || review.getPatient().getAccount() == null || 
            !review.getPatient().getAccount().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền sửa đánh giá này");
        }

        long hours = java.time.temporal.ChronoUnit.HOURS.between(review.getCreatedAt(), LocalDateTime.now());
        if (hours >= 24) {
            throw new RuntimeException("Chỉ có thể sửa đánh giá trong vòng 24 giờ sau khi gửi");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);
        doctorReviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<DoctorFeedbackResponse> getMyDoctorFeedbacks(String email) {
        return doctorReviewRepository.findByPatient_Account_EmailOrderByCreatedAtDesc(email).stream()
                .map(doctorFeedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void replyDoctorFeedback(Integer reviewId, String reply, String email) {
        DoctorReview review = doctorReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Doctor review not found"));
        review.setReply(reply);
        review.setRepliedAt(LocalDateTime.now());
        // Simple logic for staff fetching (in real app, map email to Staff)
        Staff staff = staffRepository.findAll().stream()
                .filter(s -> s.getAccount() != null && s.getAccount().getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        review.setRepliedBy(staff);
        doctorReviewRepository.save(review);
    }
}
