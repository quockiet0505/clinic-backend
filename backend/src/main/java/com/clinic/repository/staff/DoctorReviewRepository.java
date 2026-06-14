package com.clinic.repository.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.clinic.entity.staff.DoctorReview;

@Repository
public interface DoctorReviewRepository extends JpaRepository<DoctorReview, Integer> {
    
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM DoctorReview r WHERE r.doctorId = :doctorId")
    Double getAverageRatingByDoctorId(Integer doctorId);
}
