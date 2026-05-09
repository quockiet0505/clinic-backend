package com.clinic.mapper.appointment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.appointment.AppointmentRequest;
import com.clinic.dto.appointment.AppointmentResponse;
import com.clinic.entity.appointment.Appointment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentMapper {
    
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "mainDoctor", ignore = true)
    Appointment toEntity(AppointmentRequest request);

    @Mapping(source = "patient.patientId", target = "patientId")
    @Mapping(source = "patient.fullName", target = "patientName")
    @Mapping(source = "mainDoctor.staffId", target = "mainDoctorId")
    @Mapping(source = "mainDoctor.fullName", target = "doctorName")
    AppointmentResponse toResponse(Appointment appointment);
}