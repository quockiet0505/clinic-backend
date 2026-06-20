package com.clinic.repository.crm;

import com.clinic.entity.crm.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer>, JpaSpecificationExecutor<Feedback> {
    boolean existsByMedicalRecord_RecordId(Integer recordId);
    
    java.util.List<Feedback> findByMedicalRecord_Patient_Account_EmailOrderByCreatedAtDesc(String email);
}