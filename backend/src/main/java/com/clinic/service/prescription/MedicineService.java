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

    /**
     * Creates a new medicine entry in the system.
     */
    @Transactional
    public MedicineResponse create(MedicineRequest request) {
        Medicine medicine = medicineMapper.toEntity(request);
        medicine.setIsDeleted(0); // Ensure it's active upon creation
        
        return medicineMapper.toResponse(medicineRepository.save(medicine));
    }

    /**
     * Retrieves a list of all active (non-deleted) medicines.
     */
    @Transactional(readOnly = true)
    public List<MedicineResponse> getAllActive() {
        return medicineRepository.findAll().stream()
                .filter(m -> m.getIsDeleted() == 0) // Filter out soft-deleted items
                .map(medicineMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing medicine's details.
     */
    @Transactional
    public MedicineResponse update(Integer id, MedicineRequest request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found."));

        // Update allowed fields based on schema v4
        medicine.setName(request.getName());
        medicine.setActiveElement(request.getActiveElement());
        medicine.setPackingStandard(request.getPackingStandard());
        medicine.setBaseUnit(request.getBaseUnit());
        medicine.setSellPrice(request.getSellPrice());
        medicine.setUsageNote(request.getUsageNote());

        return medicineMapper.toResponse(medicineRepository.save(medicine));
    }

    /**
     * Soft deletes a medicine by setting isDeleted to 1.
     */
    @Transactional
    public void softDelete(Integer id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found."));
        
        medicine.setIsDeleted(1);
        medicineRepository.save(medicine);
    }
}