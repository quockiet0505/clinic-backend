package com.clinic.mapper.medical;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.medical.MedicalRecordVitalRequest;
import com.clinic.dto.medical.MedicalRecordVitalResponse;
import com.clinic.entity.medical.MedicalRecordVital;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MedicalRecordVitalMapper {
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "recordedBy", ignore = true)
    MedicalRecordVital toEntity(MedicalRecordVitalRequest request);

    @Mapping(source = "medicalRecord.recordId", target = "recordId")
    @Mapping(source = "recordedBy.staffId", target = "recordedById")
    @Mapping(source = "recordedBy.fullName", target = "recordedByName")
    MedicalRecordVitalResponse toResponse(MedicalRecordVital vital);
}