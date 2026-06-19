package com.clinic.repository.crm;

import com.clinic.entity.crm.DoctorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorReviewRepository extends JpaRepository<DoctorReview, Integer>, JpaSpecificationExecutor<DoctorReview> {
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM DoctorReview r WHERE r.doctor.staffId = :doctorId")
    Double getAverageRatingByDoctorId(@Param("doctorId") Integer doctorId);
}