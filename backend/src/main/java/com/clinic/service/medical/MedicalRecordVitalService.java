package com.clinic.service.medical;

import com.clinic.dto.medical.MedicalRecordVitalRequest;
import com.clinic.dto.medical.MedicalRecordVitalResponse;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.medical.MedicalRecordVital;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.medical.MedicalRecordVitalMapper;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.medical.MedicalRecordVitalRepository;
import com.clinic.repository.staff.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicalRecordVitalService {
    private final MedicalRecordVitalRepository vitalRepository;
    private final MedicalRecordRepository recordRepository;
    private final StaffRepository staffRepository;
    private final MedicalRecordVitalMapper vitalMapper;

    @Transactional
    public MedicalRecordVitalResponse saveOrUpdate(MedicalRecordVitalRequest request) {
        MedicalRecord record = recordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RuntimeException("Medical record not found"));
        
        Staff recordedBy = staffRepository.findById(request.getRecordedById())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        MedicalRecordVital vital = vitalRepository.findById(request.getRecordId())
                .orElse(new MedicalRecordVital());

        vital.setMedicalRecord(record);
        vital.setWeight(request.getWeight());
        vital.setBloodPressure(request.getBloodPressure());
        vital.setPulse(request.getPulse());
        vital.setRecordedBy(recordedBy);

        return vitalMapper.toResponse(vitalRepository.save(vital));
    }
    
    @Transactional(readOnly = true)
    public MedicalRecordVitalResponse getByRecordId(Integer recordId) {
        MedicalRecordVital vital = vitalRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Vitals not found for this record"));
        return vitalMapper.toResponse(vital);
    }

    @Transactional(readOnly = true)
    public MedicalRecordVitalResponse getLatestByEmail(String email) {
        MedicalRecordVital vital = vitalRepository.findLatestByPatientEmail(email)
                .orElseThrow(() -> new RuntimeException("No vitals found for patient"));
        return vitalMapper.toResponse(vital);
    }
}