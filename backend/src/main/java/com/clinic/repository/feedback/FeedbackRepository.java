package com.clinic.repository.feedback;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.feedback.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    Optional<Feedback> findByMedicalRecord_RecordId(Integer recordId);
}