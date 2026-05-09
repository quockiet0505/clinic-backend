package com.clinic.repository.medical;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.medical.MedicalRecordVital;

public interface MedicalRecordVitalRepository extends JpaRepository<MedicalRecordVital, Integer> {
}