package com.clinic.service.crm;

import com.clinic.dto.crm.DoctorFeedbackResponse;
import com.clinic.entity.crm.DoctorReview;
import com.clinic.entity.staff.Staff;
import com.clinic.repository.crm.DoctorReviewRepository;
import com.clinic.repository.staff.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorFeedbackService {

    private final DoctorReviewRepository doctorReviewRepository;
    private final StaffRepository staffRepository;

    public List<DoctorFeedbackResponse> getDoctorFeedbacks(String search, Integer rating, String fromDate, String toDate) {
        List<DoctorReview> reviews = doctorReviewRepository.findAll();

        // Lọc theo khoảng ngày
        if (fromDate != null && !fromDate.isEmpty() || toDate != null && !toDate.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate from = (fromDate != null && !fromDate.isEmpty()) ? LocalDate.parse(fromDate, formatter) : null;
            LocalDate to = (toDate != null && !toDate.isEmpty()) ? LocalDate.parse(toDate, formatter) : null;

            reviews = reviews.stream()
                .filter(r -> {
                    LocalDate createdDate = r.getCreatedAt().toLocalDate();
                    if (from != null && createdDate.isBefore(from)) return false;
                    if (to != null && createdDate.isAfter(to)) return false;
                    return true;
                })
                .collect(Collectors.toList());
        }

        // Lọc theo rating
        if (rating != null) {
            reviews = reviews.stream()
                .filter(r -> r.getRating().equals(rating))
                .collect(Collectors.toList());
        }

        // Lọc theo search (tên bệnh nhân hoặc bác sĩ)
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            reviews = reviews.stream()
                .filter(r -> {
                    String patientName = r.getPatient() != null ? r.getPatient().getFullName() : "";
                    String doctorName = r.getDoctor() != null ? r.getDoctor().getFullName() : "";
                    return patientName.toLowerCase().contains(searchLower) || doctorName.toLowerCase().contains(searchLower);
                })
                .collect(Collectors.toList());
        }

        return reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DoctorFeedbackResponse mapToResponse(DoctorReview review) {
        DoctorFeedbackResponse resp = new DoctorFeedbackResponse();
        resp.setReviewId(review.getReviewId());
        if (review.getDoctor() != null) {
            resp.setDoctorId(review.getDoctor().getStaffId());
            resp.setDoctorName(review.getDoctor().getFullName());
        }
        if (review.getPatient() != null) {
            resp.setPatientId(review.getPatient().getPatientId());
            resp.setPatientName(review.getPatient().getFullName());
        }
        resp.setRating(review.getRating());
        resp.setComment(review.getComment());
        resp.setCreatedAt(review.getCreatedAt());
        resp.setReply(review.getReply());
        resp.setRepliedAt(review.getRepliedAt());
        if (review.getRepliedBy() != null) {
            resp.setRepliedBy(review.getRepliedBy().getFullName());
        }
        return resp;
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