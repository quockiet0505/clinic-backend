package com.clinic.repository.patient;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.patient.Patient;

public interface PatientRepository extends JpaRepository<Patient, Integer> {
    List<Patient> findByIsDeleted(Integer isDeleted);
}