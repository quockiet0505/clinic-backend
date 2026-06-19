package com.clinic.service.crm;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.FollowUpStatus;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.crm.FollowUpFilterRequest;
import com.clinic.dto.crm.FollowUpRequest;
import com.clinic.dto.crm.FollowUpResponse;
import com.clinic.entity.crm.FollowUp;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.crm.FollowUpMapper;
import com.clinic.repository.crm.FollowUpRepository;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.specification.crm.FollowUpSpecification;
import com.clinic.util.FilterUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final FollowUpMapper followUpMapper;

    /**
     * Create a follow-up schedule for a patient after a medical examination.
     */
    @Transactional
    public FollowUpResponse create(FollowUpRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RuntimeException("Medical Record not found."));
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found."));
        Staff doctor = staffRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found."));

        FollowUp followUp = followUpMapper.toEntity(request);
        followUp.setMedicalRecord(record);
        followUp.setPatient(patient);
        followUp.setDoctor(doctor);
        followUp.setStatus(FollowUpStatus.PENDING);

        return followUpMapper.toResponse(followUpRepository.save(followUp));
    }

    @Transactional(readOnly = true)
    public PageResponse<FollowUpResponse> getAll(FollowUpFilterRequest filter) {
        Specification<FollowUp> spec = FollowUpSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<FollowUp> page = followUpRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(followUpMapper::toResponse));
    }

    /**
     * Get all follow-up schedules.
     */
    @Transactional(readOnly = true)
    public List<FollowUpResponse> getAll() {
        return followUpRepository.findAll().stream()
                .map(followUpMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update follow-up status (e.g., CONFIRMED, COMPLETED, CANCELLED).
     */
    @Transactional
    public FollowUpResponse updateStatus(Integer id, FollowUpStatus newStatus) {
        FollowUp followUp = followUpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Follow-up schedule not found."));
        
        followUp.setStatus(newStatus);
        return followUpMapper.toResponse(followUpRepository.save(followUp));
    }
}