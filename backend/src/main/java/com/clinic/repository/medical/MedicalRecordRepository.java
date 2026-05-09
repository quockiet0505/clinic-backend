package com.clinic.repository.medical;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.medical.MedicalRecord;
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Integer> {
    List<MedicalRecord> findByPatient_PatientId(Integer patientId);
}
// MedicalRecordVitalRepository extends JpaRepository<MedicalRecordVital, Integer> {}