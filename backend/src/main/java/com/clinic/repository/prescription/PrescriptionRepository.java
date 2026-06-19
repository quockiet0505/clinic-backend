package com.clinic.repository.prescription;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.clinic.entity.prescription.Prescription;

public interface PrescriptionRepository extends JpaRepository<Prescription, Integer>, JpaSpecificationExecutor<Prescription> {
    
    @Query("SELECT p FROM Prescription p WHERE p.medicalRecord.recordId = :recordId")
    Optional<Prescription> findByMedicalRecordId(@Param("recordId") Integer recordId);

    List<Prescription> findByMedicalRecord_Patient_PatientId(Integer patientId);
    
    List<Prescription> findByMedicalRecord_RecordId(Integer recordId);
}