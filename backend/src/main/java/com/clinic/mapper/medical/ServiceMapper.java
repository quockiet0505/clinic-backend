package com.clinic.mapper.medical;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.medical.ServiceRequest;
import com.clinic.dto.medical.ServiceResponse;
import com.clinic.entity.medical.Service;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceMapper {
    Service toEntity(ServiceRequest request);
    ServiceResponse toResponse(Service service);
}