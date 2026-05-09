package com.clinic.service.followup;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.FollowUpStatus;
import com.clinic.dto.followup.FollowUpRequest;
import com.clinic.dto.followup.FollowUpResponse;
import com.clinic.entity.followup.FollowUp;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.followup.FollowUpMapper;
import com.clinic.repository.followup.FollowUpRepository;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.staff.StaffRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowUpService {
    private final FollowUpRepository followUpRepository;
    private final MedicalRecordRepository recordRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final FollowUpMapper followUpMapper;

    @Transactional
    public FollowUpResponse create(FollowUpRequest request) {
        MedicalRecord record = recordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RuntimeException("Medical record not found"));
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Staff doctor = staffRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        FollowUp followUp = followUpMapper.toEntity(request);
        followUp.setMedicalRecord(record);
        followUp.setPatient(patient);
        followUp.setDoctor(doctor);
        followUp.setStatus(FollowUpStatus.PENDING);

        return followUpMapper.toResponse(followUpRepository.save(followUp));
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> getByPatientId(Integer patientId) {
        return followUpRepository.findByPatient_PatientId(patientId).stream()
                .map(followUpMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateStatus(Integer id, FollowUpStatus status) {
        FollowUp followUp = followUpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Follow-up not found"));
        followUp.setStatus(status);
        followUpRepository.save(followUp);
    }
}