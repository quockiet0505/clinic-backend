package com.clinic.service.prescription;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.dto.prescription.MedicineRequest;
import com.clinic.dto.prescription.MedicineResponse;
import com.clinic.entity.prescription.Medicine;
import com.clinic.mapper.prescription.MedicineMapper;
import com.clinic.repository.prescription.MedicineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;

    @Transactional
    public MedicineResponse create(MedicineRequest request) {
        // Validate dates logically
        if (request.getExp().isBefore(request.getMfg())) {
            throw new RuntimeException("Expiration date cannot be before Manufacturing date.");
        }
        Medicine medicine = medicineMapper.toEntity(request);
        return medicineMapper.toResponse(medicineRepository.save(medicine));
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> getAllActive() {
        return medicineRepository.findByIsDeleted(0).stream()
                .map(medicineMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicineResponse update(Integer id, MedicineRequest request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        if (request.getExp().isBefore(request.getMfg())) {
            throw new RuntimeException("Expiration date cannot be before Manufacturing date.");
        }

        medicine.setName(request.getName());
        medicine.setUnit(request.getUnit());
        medicine.setPrice(request.getPrice());
        medicine.setQuantity(request.getQuantity());
        medicine.setUsageNote(request.getUsageNote());
        medicine.setActiveElement(request.getActiveElement());
        medicine.setProductionUnit(request.getProductionUnit());
        medicine.setMfg(request.getMfg());
        medicine.setExp(request.getExp());

        return medicineMapper.toResponse(medicineRepository.save(medicine));
    }

    @Transactional
    public void softDelete(Integer id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));
        medicine.setIsDeleted(1);
        medicineRepository.save(medicine);
    }
}