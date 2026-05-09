package com.clinic.mapper.followup;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.followup.FollowUpRequest;
import com.clinic.dto.followup.FollowUpResponse;
import com.clinic.entity.followup.FollowUp;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FollowUpMapper {
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    FollowUp toEntity(FollowUpRequest request);

    @Mapping(source = "medicalRecord.recordId", target = "recordId")
    @Mapping(source = "patient.patientId", target = "patientId")
    @Mapping(source = "patient.fullName", target = "patientName")
    @Mapping(source = "doctor.staffId", target = "doctorId")
    @Mapping(source = "doctor.fullName", target = "doctorName")
    FollowUpResponse toResponse(FollowUp followUp);
}