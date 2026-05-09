package com.clinic.mapper.service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.service.ServiceOrderRequest;
import com.clinic.dto.service.ServiceOrderResponse;
import com.clinic.entity.service.ServiceOrder;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceOrderMapper {
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "orderedBy", ignore = true)
    ServiceOrder toEntity(ServiceOrderRequest request);

    @Mapping(source = "medicalRecord.recordId", target = "recordId")
    @Mapping(source = "service.serviceId", target = "serviceId")
    @Mapping(source = "service.serviceName", target = "serviceName")
    @Mapping(source = "orderedBy.staffId", target = "orderedById")
    @Mapping(source = "orderedBy.fullName", target = "orderedByName")
    ServiceOrderResponse toResponse(ServiceOrder order);
}