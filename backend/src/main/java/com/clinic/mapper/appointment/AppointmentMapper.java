package com.clinic.mapper.appointment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.clinic.dto.appointment.AppointmentRequest;
import com.clinic.dto.appointment.AppointmentResponse;
import com.clinic.entity.appointment.Appointment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface AppointmentMapper {

    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "mainDoctor", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "expertise", ignore = true)
    @Mapping(target = "suggestedExpertise", ignore = true)
    @Mapping(target = "appointmentId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Appointment toEntity(AppointmentRequest request);

    @Mapping(source = "patient.patientId", target = "patientId")
    @Mapping(source = "patient.fullName", target = "patientName")
    @Mapping(source = "mainDoctor.staffId", target = "mainDoctorId")
    @Mapping(source = "mainDoctor.fullName", target = "doctorName")
    @Mapping(source = "mainDoctor.imageUrl", target = "doctorImageUrl")
    @Mapping(source = "expertise.expertiseId", target = "expertiseId")
    @Mapping(source = "expertise.expertiseName", target = "expertiseName")
    @Mapping(source = "suggestedExpertise.expertiseId", target = "suggestedExpertiseId")
    @Mapping(source = "suggestedExpertise.expertiseName", target = "suggestedExpertiseName")
    @Mapping(source = "service.serviceId", target = "serviceId")
    @Mapping(source = "service.serviceName", target = "serviceName")
    @Mapping(source = "service.serviceType", target = "serviceType")
    AppointmentResponse toResponse(Appointment appointment);

    @org.mapstruct.AfterMapping
    default void fallbackExpertise(Appointment appointment, @org.mapstruct.MappingTarget AppointmentResponse response) {
        if (response.getExpertiseName() == null && appointment.getMainDoctor() != null && appointment.getMainDoctor().getExpertise() != null) {
            response.setExpertiseName(appointment.getMainDoctor().getExpertise().getExpertiseName());
            response.setExpertiseId(appointment.getMainDoctor().getExpertise().getExpertiseId());
        }
    }
}
