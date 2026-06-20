package com.clinic.mapper.medical;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.medical.ServiceOrderRequest;
import com.clinic.dto.medical.ServiceOrderResponse;
import com.clinic.entity.medical.ServiceOrder;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceOrderMapper {

    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "orderedBy", ignore = true)
    @Mapping(target = "sampleCollectedBy", ignore = true)
    ServiceOrder toEntity(ServiceOrderRequest request);

    @Mapping(source = "medicalRecord.recordId", target = "recordId")
    @Mapping(source = "medicalRecord.patient.fullName", target = "patientName")
    @Mapping(source = "service.serviceId", target = "serviceId")
    @Mapping(source = "service.serviceName", target = "serviceName")
    @Mapping(source = "service.originalPrice", target = "price")
    @Mapping(source = "orderedBy.staffId", target = "orderedById")
    @Mapping(source = "orderedBy.fullName", target = "orderedByName")
    @Mapping(source = "orderedBy.fullName", target = "doctorName")
    @Mapping(source = "sampleCollectedBy.staffId", target = "sampleCollectedById")
    @Mapping(source = "sampleCollectedBy.fullName", target = "sampleCollectedByName")
    ServiceOrderResponse toResponse(ServiceOrder serviceOrder);
}