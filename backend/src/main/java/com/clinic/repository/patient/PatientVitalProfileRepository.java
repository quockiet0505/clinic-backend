package com.clinic.repository.patient;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.patient.PatientVitalProfile;

public interface PatientVitalProfileRepository extends JpaRepository<PatientVitalProfile, Integer> {
}