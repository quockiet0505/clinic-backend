package com.clinic.mapper.medical;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.medical.ServiceResultRequest;
import com.clinic.dto.medical.ServiceResultResponse;
import com.clinic.entity.medical.ServiceResult;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceResultMapper {
    @Mapping(target = "serviceOrder", ignore = true)
    @Mapping(target = "enteredBy", ignore = true)
    ServiceResult toEntity(ServiceResultRequest request);

    @Mapping(source = "serviceOrder.orderId", target = "orderId")
    @Mapping(source = "serviceOrder.service.serviceName", target = "serviceName")
    @Mapping(source = "enteredBy.staffId", target = "enteredById")
    @Mapping(source = "enteredBy.fullName", target = "enteredByName")
    ServiceResultResponse toResponse(ServiceResult result);
}