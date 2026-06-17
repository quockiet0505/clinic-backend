package com.clinic.repository.staff;

import com.clinic.entity.crm.DoctorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffDoctorReviewRepository extends JpaRepository<DoctorReview, Integer> {

    // Sửa HQL: r.doctor.staffId thay vì r.doctorId
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM DoctorReview r WHERE r.doctor.staffId = :doctorId")
    Double getAverageRatingByDoctorId(Integer doctorId);
}