package com.clinic.repository.crm;

import com.clinic.entity.crm.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface FeedbackRepository extends JpaRepository<Feedback, Integer>, JpaSpecificationExecutor<Feedback> {
    boolean existsByMedicalRecord_RecordId(Integer recordId);
    
    List<Feedback> findByMedicalRecord_Patient_Account_EmailOrderByCreatedAtDesc(String email);

    @Query("SELECT f FROM Feedback f WHERE f.aiStatus = 'APPROVED' AND f.rating >= :minRating ORDER BY f.createdAt DESC")
    List<Feedback> findLandingFeedbacks(@Param("minRating") int minRating, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COALESCE(AVG(f.rating), 0.0) FROM Feedback f WHERE f.aiStatus = 'APPROVED'")
    Double getAverageApprovedRating();

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.aiStatus = :status")
    long countByAiStatus(@Param("status") String status);

    @Query("SELECT f FROM Feedback f WHERE f.aiStatus = 'PENDING' AND f.createdAt < :cutoff")
    List<Feedback> findPendingOlderThan(@Param("cutoff") LocalDateTime cutoff);

    List<Feedback> findByAiStatus(String status);
}