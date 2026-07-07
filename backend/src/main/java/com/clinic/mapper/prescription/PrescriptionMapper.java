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

@Mapper(componentModel = "spring")
public interface PrescriptionMapper {
    @Mapping(target = "medicalRecord", ignore = true)
    Prescription toEntity(PrescriptionRequest request);

    @Mapping(source = "medicalRecord.recordId", target = "recordId")
    @Mapping(source = "medicalRecord.patient.patientId", target = "patientId")
    @Mapping(source = "medicalRecord.patient.fullName", target = "patientName")
    @Mapping(source = "medicalRecord.mainDoctor.fullName", target = "doctorName")
    @Mapping(source = "medicalRecord.diagnosis", target = "diagnosis")
    @Mapping(source = "medicalRecord.consultationFinalFee", target = "consultationFinalFee")
    PrescriptionResponse toResponse(Prescription prescription);

    @Mapping(target = "prescription", ignore = true)
    @Mapping(target = "medicine", ignore = true)
    @Mapping(target = "id", ignore = true)
    PrescriptionItem toItemEntity(PrescriptionItemRequest request);

    @Mapping(source = "medicine.medicineId", target = "medicineId")
    @Mapping(source = "medicine.name", target = "medicineName")
    PrescriptionItemResponse toItemResponse(PrescriptionItem item);
}