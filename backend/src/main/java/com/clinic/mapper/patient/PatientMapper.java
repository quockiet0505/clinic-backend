package com.clinic.mapper.patient;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.patient.PatientRequest;
import com.clinic.dto.patient.PatientResponse;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.patient.PatientVitalProfile;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PatientMapper {
    
    @Mapping(target = "account", ignore = true)
    Patient toEntity(PatientRequest request);

    // FIXED: Explicitly mapping sources to avoid ambiguity
    @Mapping(source = "patient.patientId", target = "patientId")
    @Mapping(source = "patient.account.accountId", target = "accountId")
    @Mapping(source = "patient.account.email", target = "email")
    @Mapping(source = "patient.fullName", target = "fullName")
    @Mapping(source = "patient.gender", target = "gender")
    @Mapping(source = "patient.dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "patient.phone", target = "phone")
    @Mapping(source = "patient.address", target = "address")
    @Mapping(source = "vitalProfile.height", target = "height")
    @Mapping(source = "vitalProfile.bloodType", target = "bloodType")
    @Mapping(source = "vitalProfile.allergies", target = "allergies")
    @Mapping(source = "vitalProfile.medicalHistory", target = "medicalHistory")
    PatientResponse toResponse(Patient patient, PatientVitalProfile vitalProfile);
}