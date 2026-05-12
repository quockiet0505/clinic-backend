package com.clinic.mapper.prescription;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.prescription.PrescriptionRequest;
import com.clinic.dto.prescription.PrescriptionResponse;
import com.clinic.dto.prescription.PrescriptionItemRequest;
import com.clinic.dto.prescription.PrescriptionItemResponse;
import com.clinic.entity.prescription.Prescription;
import com.clinic.entity.prescription.PrescriptionItem;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PrescriptionMapper {

    // --- MAPPING CHO ĐƠN THUỐC (VỎ) ---
    @Mapping(target = "medicalRecord", ignore = true)
    Prescription toEntity(PrescriptionRequest request);

    @Mapping(source = "medicalRecord.recordId", target = "recordId")
    PrescriptionResponse toResponse(Prescription prescription);


    // --- MAPPING CHO CHI TIẾT ĐƠN THUỐC (ITEM) ---
    @Mapping(target = "prescription", ignore = true)
    @Mapping(target = "medicine", ignore = true)
    @Mapping(target = "id", ignore = true) // Bỏ qua khóa phức hợp, sẽ set thủ công ở Service
    PrescriptionItem toItemEntity(PrescriptionItemRequest request);

    @Mapping(source = "medicine.medicineId", target = "medicineId")
    @Mapping(source = "medicine.name", target = "medicineName")
    PrescriptionItemResponse toItemResponse(PrescriptionItem item);
}