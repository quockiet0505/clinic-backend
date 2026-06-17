package com.clinic.mapper.crm;

import com.clinic.dto.crm.DoctorFeedbackResponse;
import com.clinic.entity.crm.DoctorReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorFeedbackMapper {

    @Mapping(target = "repliedBy", source = "repliedBy.fullName")
    @Mapping(target = "doctorId", source = "doctor.staffId")
    @Mapping(target = "doctorName", source = "doctor.fullName")
    @Mapping(target = "patientId", source = "patient.patientId")
    @Mapping(target = "patientName", source = "patient.fullName")
    DoctorFeedbackResponse toResponse(DoctorReview doctorReview);
}