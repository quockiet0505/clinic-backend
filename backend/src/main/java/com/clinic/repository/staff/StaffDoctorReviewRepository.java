package com.clinic.repository.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.clinic.entity.crm.DoctorReview;

@Repository
public interface StaffDoctorReviewRepository extends JpaRepository<DoctorReview, Integer>, JpaSpecificationExecutor<DoctorReview> {

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM DoctorReview r WHERE r.doctor.staffId = :doctorId")
    Double getAverageRatingByDoctorId(Integer doctorId);
}