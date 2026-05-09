package com.clinic.mapper.service;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.service.ServiceRequest;
import com.clinic.dto.service.ServiceResponse;
import com.clinic.entity.service.Service;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceMapper {
    Service toEntity(ServiceRequest request);
    ServiceResponse toResponse(Service service);
}