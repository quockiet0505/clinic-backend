package com.clinic.repository.patient;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.patient.Patient;

public interface PatientRepository extends JpaRepository<Patient, Integer> {
    List<Patient> findByIsDeleted(Integer isDeleted);
    Optional<Patient> findByAccount_AccountId(Integer accountId);

    Optional<Patient> findByAccount_Email(String email);
   
}