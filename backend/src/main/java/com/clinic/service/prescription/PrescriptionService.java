package com.clinic.service.prescription;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.dto.prescription.PrescriptionItemRequest;
import com.clinic.dto.prescription.PrescriptionRequest;
import com.clinic.dto.prescription.PrescriptionResponse;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.prescription.Medicine;
import com.clinic.entity.prescription.Prescription;
import com.clinic.entity.prescription.PrescriptionItem;
import com.clinic.mapper.prescription.PrescriptionMapper;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.prescription.MedicineRepository;
import com.clinic.repository.prescription.PrescriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository recordRepository;
    private final MedicineRepository medicineRepository;
    private final PrescriptionMapper prescriptionMapper;

    @Transactional
    public PrescriptionResponse create(PrescriptionRequest request) {
        MedicalRecord record = recordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RuntimeException("Medical Record not found."));

        // A record should typically only have one prescription
        if (prescriptionRepository.findByMedicalRecord_RecordId(record.getRecordId()).isPresent()) {
            throw new RuntimeException("A prescription already exists for this medical record.");
        }

        Prescription prescription = new Prescription();
        prescription.setMedicalRecord(record);

        // Process each medicine item
        for (PrescriptionItemRequest itemReq : request.getItems()) {
            Medicine medicine = medicineRepository.findById(itemReq.getMedicineId())
                    .orElseThrow(() -> new RuntimeException("Medicine ID " + itemReq.getMedicineId() + " not found."));

            // Logical Check: Do we have enough medicine in stock?
            if (medicine.getQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Not enough stock for medicine: " + medicine.getName() + ". Available: " + medicine.getQuantity());
            }

            PrescriptionItem item = new PrescriptionItem();
            item.setMedicine(medicine);
            item.setDosage(itemReq.getDosage());
            item.setQuantity(itemReq.getQuantity());
            
            // CRITICAL: Lock the price at the time of prescribing
            item.setPrice(medicine.getPrice()); 

            prescription.addItem(item);

            // Deduct from inventory (Optional, but highly recommended for a real clinic)
            medicine.setQuantity(medicine.getQuantity() - itemReq.getQuantity());
            medicineRepository.save(medicine);
        }

        return prescriptionMapper.toResponse(prescriptionRepository.save(prescription));
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getByRecordId(Integer recordId) {
        Prescription prescription = prescriptionRepository.findByMedicalRecord_RecordId(recordId)
                .orElseThrow(() -> new RuntimeException("Prescription not found for this medical record."));
        return prescriptionMapper.toResponse(prescription);
    }
}