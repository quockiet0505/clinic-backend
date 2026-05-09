package com.clinic.repository.crm;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.common.enums.FollowUpStatus;
import com.clinic.entity.crm.FollowUp;

public interface FollowUpRepository extends JpaRepository<FollowUp, Integer> {
    List<FollowUp> findByPatient_PatientId(Integer patientId);
    List<FollowUp> findByScheduledDatetimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, FollowUpStatus status);
}