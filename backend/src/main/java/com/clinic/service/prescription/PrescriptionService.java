package com.clinic.service.prescription;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicineRepository medicineRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final com.clinic.repository.patient.PatientRepository patientRepository;

    /**
     * Creates a new prescription linked to a medical record.
     * Maps each medicine requested and assigns the current selling price.
     */
    @Transactional
    public PrescriptionResponse create(PrescriptionRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RuntimeException("Medical record not found."));

        Prescription prescription = prescriptionMapper.toEntity(request);
        prescription.setMedicalRecord(record);
        
        // Initialize the items list to avoid NullPointerException
        if (prescription.getItems() == null) {
            prescription.setItems(new ArrayList<>());
        }

        // We must save the prescription FIRST to generate a prescription_id for the composite keys
        final Prescription savedPrescription = prescriptionRepository.save(prescription);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (PrescriptionItemRequest itemRequest : request.getItems()) {
                Medicine medicine = medicineRepository.findById(itemRequest.getMedicineId())
                        .orElseThrow(() -> new RuntimeException("Medicine not found: " + itemRequest.getMedicineId()));

                PrescriptionItem item = prescriptionMapper.toItemEntity(itemRequest);
                item.setPrescription(savedPrescription);
                item.setMedicine(medicine);
                
                // Assign the current selling price of the medicine at the time of prescription
                item.setPrice(medicine.getSellPrice());

                // Construct and set the composite primary key
                PrescriptionItemKey key = new PrescriptionItemKey(savedPrescription.getPrescriptionId(), medicine.getMedicineId());
                item.setId(key);

                savedPrescription.getItems().add(item);
            }
        }

        // Save again to cascade the items into the prescription_item table
        return prescriptionMapper.toResponse(prescriptionRepository.save(savedPrescription));
    }

    /**
     * Retrieves a specific prescription by its ID.
     */
    @Transactional(readOnly = true)
    public PrescriptionResponse getById(Integer id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found."));
        return prescriptionMapper.toResponse(prescription);
    }
    
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getAll() {
        return prescriptionRepository.findAll().stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getMyPrescriptions(String email) {
        com.clinic.entity.patient.Patient patient = patientRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
                
        return prescriptionRepository.findByMedicalRecord_Patient_PatientId(patient.getPatientId())
                .stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }
}