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

import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.prescription.PrescriptionFilterRequest;
import com.clinic.dto.prescription.PrescriptionItemRequest;
import com.clinic.dto.prescription.PrescriptionRequest;
import com.clinic.dto.prescription.PrescriptionResponse;
import com.clinic.dto.prescription.DrugInteractionWarning;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.prescription.Medicine;
import com.clinic.entity.prescription.Prescription;
import com.clinic.entity.prescription.PrescriptionItem;
import com.clinic.mapper.prescription.PrescriptionMapper;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.medical.ServiceOrderRepository;
import com.clinic.repository.prescription.DrugInteractionRepository;
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
    private final ServiceOrderRepository serviceOrderRepository;
    private final MedicineRepository medicineRepository;
    private final DrugInteractionRepository drugInteractionRepository;
    private final PatientRepository patientRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final com.clinic.service.crm.NotificationService notificationService;

    @Transactional
    public PrescriptionResponse create(PrescriptionRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RuntimeException("Medical record not found."));

        if (record.getStatus() == com.clinic.common.enums.MedicalRecordStatus.DONE) {
            throw new RuntimeException("Không thể kê đơn cho bệnh án đã hoàn thành.");
        }

        boolean hasPendingLabOrders = serviceOrderRepository.findByMedicalRecordId(record.getRecordId())
                .stream()
                .anyMatch(o -> o.getStatus() == ServiceOrderStatus.ORDERED);
        if (hasPendingLabOrders) {
            throw new RuntimeException("Không thể kê đơn khi còn chỉ định cận lâm sàng chưa có kết quả.");
        }

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            java.util.Set<Integer> uniqueIds = new java.util.HashSet<>();
            for (PrescriptionItemRequest item : request.getItems()) {
                if (item.getMedicineId() != null) {
                    if (!uniqueIds.add(item.getMedicineId())) {
                        throw new RuntimeException("Không thể chọn trùng cùng một loại thuốc.");
                    }
                    Medicine med = medicineRepository.findById(item.getMedicineId())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc."));
                    
                    if (med.getIsDeleted() == 1) {
                        throw new RuntimeException("Thuốc " + med.getName() + " không còn hoạt động.");
                    }
                }
            }

            List<Integer> medicineIds = request.getItems().stream()
                    .map(PrescriptionItemRequest::getMedicineId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
            List<DrugInteractionWarning> warnings = checkInteractions(medicineIds);
            if (!warnings.isEmpty()) {
                StringBuilder errorMsg = new StringBuilder("CẢNH BÁO TƯƠNG TÁC THUỐC NGHIÊM TRỌNG:\n");
                for (DrugInteractionWarning w : warnings) {
                    errorMsg.append(String.format("- %s và %s:\n  + Cơ chế: %s\n  + Hậu quả: %s\n  + Xử trí: %s\n\n", 
                            w.getMedicine1(), w.getMedicine2(), w.getMechanism(), w.getConsequence(), w.getManagement()));
                }
                throw new RuntimeException(errorMsg.toString());
            }
        }

        Prescription prescription = prescriptionMapper.toEntity(request);
        if (prescription.getItems() != null) {
            prescription.getItems().clear();
        }
        prescription.setMedicalRecord(record);
        prescription.setStatus("PENDING");
        prescription.setCreatedAt(LocalDateTime.now());

        Prescription saved = prescriptionRepository.save(prescription);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (PrescriptionItemRequest itemReq : request.getItems()) {
                Medicine medicine = null;
                if (itemReq.getMedicineId() != null) {
                    medicine = medicineRepository.findById(itemReq.getMedicineId()).get();
                }
                PrescriptionItem item = prescriptionMapper.toItemEntity(itemReq);
                item.setPrescription(saved);
                item.setMedicine(medicine);
                item.setMedicineName(itemReq.getMedicineName());
                item.setFrequency(itemReq.getFrequency());
                item.setDurationDays(itemReq.getDurationDays());
                saved.getItems().add(item);
            }
            prescriptionRepository.save(saved);
        }

        // Notify Patient
        if (record.getPatient() != null && record.getPatient().getAccount() != null) {
            notificationService.createAndSendNotification(
                    record.getPatient().getAccount().getAccountId(),
                    "Đơn thuốc của bạn đã sẵn sàng. Vui lòng di chuyển đến quầy Dược để nhận thuốc.",
                    "SYSTEM"
            );
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


    @Transactional(readOnly = true)
    public List<DrugInteractionWarning> checkInteractions(List<Integer> medicineIds) {
        List<DrugInteractionWarning> warnings = new ArrayList<>();
        if (medicineIds == null || medicineIds.size() < 2) return warnings;

        List<Medicine> medicines = medicineRepository.findAllById(medicineIds);
        
        for (int i = 0; i < medicines.size(); i++) {
            for (int j = i + 1; j < medicines.size(); j++) {
                Medicine m1 = medicines.get(i);
                Medicine m2 = medicines.get(j);
                
                if (m1.getActiveElement() == null || m2.getActiveElement() == null) continue;

                // Split, trim and sanitize
                String[] elements1 = m1.getActiveElement().split("[,+]");
                String[] elements2 = m2.getActiveElement().split("[,+]");

                for (String e1 : elements1) {
                    for (String e2 : elements2) {
                        String cleanE1 = e1.trim().toLowerCase();
                        String cleanE2 = e2.trim().toLowerCase();
                        
                        if (cleanE1.isEmpty() || cleanE2.isEmpty()) continue;

                        List<com.clinic.entity.prescription.DrugInteraction> interactions = 
                                drugInteractionRepository.findInteractions(cleanE1, cleanE2);
                        
                        for (com.clinic.entity.prescription.DrugInteraction interaction : interactions) {
                            DrugInteractionWarning warning = new DrugInteractionWarning(
                                    m1.getName(), m2.getName(), 
                                    interaction.getMechanism(), 
                                    interaction.getConsequence(), 
                                    interaction.getManagement());
                            
                            boolean exists = warnings.stream().anyMatch(w -> 
                                (w.getMedicine1().equals(warning.getMedicine1()) && w.getMedicine2().equals(warning.getMedicine2()))
                            );
                            if (!exists) {
                                warnings.add(warning);
                            }
                        }
                    }
                }
            }
        }
        return warnings;
    }
}