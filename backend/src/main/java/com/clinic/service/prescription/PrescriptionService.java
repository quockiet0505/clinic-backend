package com.clinic.service.prescription;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.dto.common.PageResponse;
import com.clinic.dto.prescription.PrescriptionFilterRequest;
import com.clinic.dto.prescription.PrescriptionItemRequest;
import com.clinic.dto.prescription.PrescriptionRequest;
import com.clinic.dto.prescription.PrescriptionResponse;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.prescription.Medicine;
import com.clinic.entity.prescription.Prescription;
import com.clinic.entity.prescription.PrescriptionItem;
import com.clinic.entity.prescription.PrescriptionItemKey;
import com.clinic.mapper.prescription.PrescriptionMapper;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.prescription.MedicineRepository;
import com.clinic.repository.prescription.PrescriptionRepository;
import com.clinic.specification.prescription.PrescriptionSpecification;
import com.clinic.entity.patient.Patient;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.util.FilterUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicineRepository medicineRepository;
    private final PatientRepository patientRepository;
    private final PrescriptionMapper prescriptionMapper;

    @Transactional
    public PrescriptionResponse create(PrescriptionRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RuntimeException("Medical record not found."));

        Prescription prescription = prescriptionMapper.toEntity(request);
        prescription.setMedicalRecord(record);
        prescription.setStatus("PENDING");
        prescription.setCreatedAt(LocalDateTime.now());

        Prescription saved = prescriptionRepository.save(prescription);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (PrescriptionItemRequest itemReq : request.getItems()) {
                Medicine medicine = medicineRepository.findById(itemReq.getMedicineId())
                        .orElseThrow(() -> new RuntimeException("Medicine not found: " + itemReq.getMedicineId()));
                PrescriptionItem item = prescriptionMapper.toItemEntity(itemReq);
                item.setPrescription(saved);
                item.setMedicine(medicine);
                item.setId(new PrescriptionItemKey(saved.getPrescriptionId(), medicine.getMedicineId()));
                saved.getItems().add(item);
            }
            prescriptionRepository.save(saved);
        }

        return prescriptionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getById(Integer id) {
        return prescriptionRepository.findById(id)
                .map(prescriptionMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Prescription not found."));
    }

    @Transactional(readOnly = true)
    public PageResponse<PrescriptionResponse> getAll(PrescriptionFilterRequest filter) {
        Specification<Prescription> spec = PrescriptionSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<Prescription> page = prescriptionRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(prescriptionMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getAll() {
        return prescriptionRepository.findAll().stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getMyPrescriptions(String email) {
        Patient patient = patientRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return prescriptionRepository.findByMedicalRecord_Patient_PatientIdOrderByCreatedAtDesc(patient.getPatientId())
                .stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void dispense(Integer id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found."));
        prescription.setStatus("DISPENSED");
        prescriptionRepository.save(prescription);
    }
}