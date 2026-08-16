package com.clinic.repository.crm;

import com.clinic.entity.crm.DoctorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface DoctorReviewRepository extends JpaRepository<DoctorReview, Integer>, JpaSpecificationExecutor<DoctorReview> {
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM DoctorReview r WHERE r.doctor.staffId = :doctorId")
    Double getAverageRatingByDoctorId(@Param("doctorId") Integer doctorId);

    boolean existsByAppointment_AppointmentId(Integer appointmentId);
    
    List<DoctorReview> findByPatient_Account_EmailOrderByCreatedAtDesc(String email);

    @Query("SELECT r FROM DoctorReview r WHERE r.aiStatus = 'APPROVED' AND r.rating >= :minRating ORDER BY r.createdAt DESC")
    List<DoctorReview> findLandingReviews(@Param("minRating") int minRating, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM DoctorReview r WHERE r.aiStatus = 'APPROVED'")
    Double getAverageApprovedRating();

    @Query("SELECT COUNT(r) FROM DoctorReview r WHERE r.aiStatus = :status")
    long countByAiStatus(@Param("status") String status);

    @Query("SELECT r FROM DoctorReview r WHERE r.aiStatus = 'PENDING' AND r.createdAt < :cutoff")
    List<DoctorReview> findPendingOlderThan(@Param("cutoff") LocalDateTime cutoff);

    List<DoctorReview> findByAiStatus(String status);
}
