package com.clinic.service.crm;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.dto.crm.FeedbackRequest;
import com.clinic.entity.crm.Feedback;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.repository.crm.FeedbackRepository;
import com.clinic.repository.medical.MedicalRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final MedicalRecordRepository recordRepository;

    @Transactional
    public Feedback create(FeedbackRequest request) {
        MedicalRecord record = recordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RuntimeException("Medical record not found"));

        if (feedbackRepository.findByMedicalRecord_RecordId(request.getRecordId()).isPresent()) {
            throw new RuntimeException("Feedback already submitted for this record");
        }

        Feedback feedback = new Feedback();
        feedback.setMedicalRecord(record);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());

        return feedbackRepository.save(feedback);
    }
}