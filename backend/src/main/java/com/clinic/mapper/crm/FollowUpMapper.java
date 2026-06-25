package com.clinic.mapper.crm;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.crm.FollowUpRequest;
import com.clinic.dto.crm.FollowUpResponse;
import com.clinic.entity.crm.FollowUp;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FollowUpMapper {
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "confirmedAt", ignore = true)
    @Mapping(target = "reminderSentAt", ignore = true)
    @Mapping(target = "cancelReason", ignore = true)
    FollowUp toEntity(FollowUpRequest request);

    @Mapping(source = "medicalRecord.recordId", target = "recordId")
    @Mapping(source = "patient.patientId", target = "patientId")
    @Mapping(source = "patient.fullName", target = "patientName")
    @Mapping(source = "patient.account.accountId", target = "accountId")
    @Mapping(source = "patient.phone", target = "phone")
    @Mapping(source = "doctor.staffId", target = "doctorId")
    @Mapping(source = "doctor.fullName", target = "doctorName")
    @Mapping(source = "appointment.appointmentId", target = "appointmentId")
    FollowUpResponse toResponse(FollowUp followUp);
}