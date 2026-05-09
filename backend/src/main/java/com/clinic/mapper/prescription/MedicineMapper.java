package com.clinic.mapper.prescription;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.prescription.MedicineRequest;
import com.clinic.dto.prescription.MedicineResponse;
import com.clinic.entity.prescription.Medicine;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MedicineMapper {
    Medicine toEntity(MedicineRequest request);
    MedicineResponse toResponse(Medicine medicine);
}