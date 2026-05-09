package com.clinic.repository.crm;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.crm.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    Optional<Feedback> findByMedicalRecord_RecordId(Integer recordId);
}