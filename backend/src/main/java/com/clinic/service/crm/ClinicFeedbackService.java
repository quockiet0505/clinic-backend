package com.clinic.service.crm;

import com.clinic.dto.crm.ClinicFeedbackResponse;
import com.clinic.entity.crm.Feedback;
import com.clinic.entity.medical.MedicalRecord; // ✅ đúng package
import com.clinic.entity.staff.Staff;
import com.clinic.repository.crm.FeedbackRepository;
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
public class ClinicFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final StaffRepository staffRepository;
    // Không cần MedicalRecordRepository vì chỉ dùng để map dữ liệu từ Feedback entity

    public List<ClinicFeedbackResponse> getClinicFeedbacks(String search, Integer rating, String fromDate, String toDate) {
        List<Feedback> feedbacks = feedbackRepository.findAll();

        // Lọc theo khoảng ngày
        if (fromDate != null && !fromDate.isEmpty() || toDate != null && !toDate.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate from = (fromDate != null && !fromDate.isEmpty()) ? LocalDate.parse(fromDate, formatter) : null;
            LocalDate to = (toDate != null && !toDate.isEmpty()) ? LocalDate.parse(toDate, formatter) : null;

            feedbacks = feedbacks.stream()
                .filter(fb -> {
                    LocalDate createdDate = fb.getCreatedAt().toLocalDate();
                    if (from != null && createdDate.isBefore(from)) return false;
                    if (to != null && createdDate.isAfter(to)) return false;
                    return true;
                })
                .collect(Collectors.toList());
        }

        // Lọc theo rating
        if (rating != null) {
            feedbacks = feedbacks.stream()
                .filter(fb -> fb.getRating().equals(rating))
                .collect(Collectors.toList());
        }

        // Lọc theo search (tên bệnh nhân)
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            feedbacks = feedbacks.stream()
                .filter(fb -> {
                    MedicalRecord record = fb.getMedicalRecord();
                    String patientName = (record != null && record.getPatient() != null) 
                        ? record.getPatient().getFullName() 
                        : "";
                    return patientName.toLowerCase().contains(searchLower);
                })
                .collect(Collectors.toList());
        }

        return feedbacks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ClinicFeedbackResponse mapToResponse(Feedback fb) {
        ClinicFeedbackResponse resp = new ClinicFeedbackResponse();
        resp.setFeedbackId(fb.getFeedbackId());
        MedicalRecord record = fb.getMedicalRecord();
        if (record != null) {
            resp.setRecordId(record.getRecordId());
            if (record.getPatient() != null) {
                resp.setPatientName(record.getPatient().getFullName());
            }
        }
        resp.setRating(fb.getRating());
        resp.setComment(fb.getComment());
        resp.setCreatedAt(fb.getCreatedAt());
        resp.setReply(fb.getReply());
        resp.setRepliedAt(fb.getRepliedAt());
        if (fb.getRepliedBy() != null) {
            resp.setRepliedBy(fb.getRepliedBy().getFullName());
        }
        return resp;
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