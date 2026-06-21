package com.clinic.mapper.crm;

import com.clinic.dto.crm.ClinicFeedbackResponse;
import com.clinic.entity.crm.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClinicFeedbackMapper {

    @Mapping(target = "repliedBy", source = "repliedBy.fullName")
    @Mapping(target = "recordId", source = "medicalRecord.recordId")
    @Mapping(target = "appointmentId", source = "medicalRecord.appointment.appointmentId")
    @Mapping(target = "patientName", source = "medicalRecord.patient.fullName")
    ClinicFeedbackResponse toResponse(Feedback feedback);
}