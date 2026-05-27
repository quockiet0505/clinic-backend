package com.clinic.repository.crm;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.clinic.common.enums.FollowUpStatus;
import com.clinic.entity.crm.FollowUp;

public interface FollowUpRepository extends JpaRepository<FollowUp, Integer> {
    // Method cho detail
    @Query("SELECT f FROM FollowUp f WHERE f.medicalRecord.recordId = :recordId")
    List<FollowUp> findByMedicalRecordId(@Param("recordId") Integer recordId);
    
    // Method cho scheduler (có thể bỏ nếu comment job)
    List<FollowUp> findByScheduledDatetimeBetweenAndStatusIn(LocalDateTime start, LocalDateTime end, List<FollowUpStatus> statuses);
}