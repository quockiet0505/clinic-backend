package com.clinic.mapper.service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.service.DoctorServicePriceRequest;
import com.clinic.dto.service.DoctorServicePriceResponse;
import com.clinic.entity.service.DoctorServicePrice;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DoctorServicePriceMapper {
    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "service", ignore = true)
    DoctorServicePrice toEntity(DoctorServicePriceRequest request);

    @Mapping(source = "staff.staffId", target = "staffId")
    @Mapping(source = "staff.fullName", target = "doctorName")
    @Mapping(source = "service.serviceId", target = "serviceId")
    @Mapping(source = "service.serviceName", target = "serviceName")
    DoctorServicePriceResponse toResponse(DoctorServicePrice entity);
}