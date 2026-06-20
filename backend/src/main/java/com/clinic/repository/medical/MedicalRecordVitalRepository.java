package com.clinic.repository.medical;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.clinic.entity.medical.MedicalRecordVital;

public interface MedicalRecordVitalRepository extends JpaRepository<MedicalRecordVital, Integer> {
    @Query("SELECT v FROM MedicalRecordVital v WHERE v.medicalRecord.patient.account.email = :email ORDER BY v.medicalRecord.createdAt DESC LIMIT 1")
    Optional<MedicalRecordVital> findLatestByPatientEmail(@Param("email") String email);
}