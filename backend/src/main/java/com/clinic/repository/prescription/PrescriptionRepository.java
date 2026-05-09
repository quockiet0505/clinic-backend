package com.clinic.repository.prescription;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.prescription.Prescription;

public interface PrescriptionRepository extends JpaRepository<Prescription, Integer> {
    Optional<Prescription> findByMedicalRecord_RecordId(Integer recordId);
}