package com.clinic.mapper.medical;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.medical.MedicalRecordRequest;
import com.clinic.dto.medical.MedicalRecordResponse;
import com.clinic.entity.medical.MedicalRecord;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MedicalRecordMapper {

    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "mainDoctor", ignore = true)
    @Mapping(target = "updatedByDoctor", ignore = true)
    MedicalRecord toEntity(MedicalRecordRequest request);

    @Mapping(source = "patient.patientId", target = "patientId")
    @Mapping(source = "patient.fullName", target = "patientName")
    @Mapping(source = "appointment.appointmentId", target = "appointmentId")
    @Mapping(source = "appointment.status", target = "appointmentStatus")
    @Mapping(source = "appointment.queueNumber", target = "queueNumber")
    @Mapping(source = "appointment.checkinTime", target = "checkinTime")
    @Mapping(source = "mainDoctor.staffId", target = "mainDoctorId")
    @Mapping(source = "mainDoctor.fullName", target = "mainDoctorName")
    @Mapping(source = "updatedByDoctor.staffId", target = "updatedByDoctorId")
    @Mapping(source = "updatedByDoctor.fullName", target = "updatedByDoctorName")
    MedicalRecordResponse toResponse(MedicalRecord medicalRecord);
}