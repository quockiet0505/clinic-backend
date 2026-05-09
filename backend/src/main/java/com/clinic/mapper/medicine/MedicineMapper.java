package com.clinic.mapper.medicine;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.medicine.MedicineRequest;
import com.clinic.dto.medicine.MedicineResponse;
import com.clinic.entity.medicine.Medicine;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MedicineMapper {
    Medicine toEntity(MedicineRequest request);
    MedicineResponse toResponse(Medicine medicine);
}