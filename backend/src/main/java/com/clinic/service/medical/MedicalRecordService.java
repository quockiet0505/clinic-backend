package com.clinic.service.medical;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.dto.medical.MedicalRecordRequest;
import com.clinic.dto.medical.MedicalRecordResponse;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.medical.MedicalRecordMapper;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.staff.StaffRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {
    
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final MedicalRecordMapper medicalRecordMapper;

    /**
     * Creates a new medical record for a patient.
     */
    @Transactional
    public MedicalRecordResponse create(MedicalRecordRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found."));
        Staff mainDoctor = staffRepository.findById(request.getMainDoctorId())
                .orElseThrow(() -> new RuntimeException("Main doctor not found."));

        MedicalRecord record = medicalRecordMapper.toEntity(request);
        record.setPatient(patient);
        record.setMainDoctor(mainDoctor);

        return medicalRecordMapper.toResponse(medicalRecordRepository.save(record));
    }

    /**
     * Retrieves all medical records.
     */
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getAll() {
        return medicalRecordRepository.findAll().stream()
                .map(medicalRecordMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing medical record and logs the doctor who made the changes.
     */
    @Transactional
    public MedicalRecordResponse update(Integer recordId, MedicalRecordRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found."));

        record.setDiagnosis(request.getDiagnosis());
        record.setTreatment(request.getTreatment());
        record.setNote(request.getNote());
        
        if (request.getStatus() != null) {
            record.setStatus(request.getStatus());
        }

        // Track which doctor updated the record and the reason for the edit
        if (request.getUpdatedByDoctorId() != null) {
            Staff updatedBy = staffRepository.findById(request.getUpdatedByDoctorId())
                    .orElseThrow(() -> new RuntimeException("Doctor tracking ID not found."));
            record.setUpdatedByDoctor(updatedBy);
            record.setEditReason(request.getEditReason());
        }

        return medicalRecordMapper.toResponse(medicalRecordRepository.save(record));
    }
}