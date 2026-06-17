package com.clinic.repository.crm;

import com.clinic.entity.crm.DoctorReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorReviewRepository extends JpaRepository<DoctorReview, Integer> {
}