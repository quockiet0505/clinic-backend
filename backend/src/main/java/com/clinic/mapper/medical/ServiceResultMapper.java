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
    @Mapping(target = "enteredAt", ignore = true)
    ServiceResult toEntity(ServiceResultRequest request);

    @Mapping(source = "serviceOrder.orderId", target = "orderId")
    @Mapping(source = "serviceOrder.service.serviceName", target = "serviceName")
    @Mapping(source = "serviceOrder.medicalRecord.patient.patientId", target = "patientId")
    @Mapping(source = "serviceOrder.medicalRecord.patient.fullName", target = "patientName")
    @Mapping(source = "serviceOrder.orderedBy.fullName", target = "doctorName")
    @Mapping(source = "enteredBy.staffId", target = "enteredById")
    @Mapping(source = "enteredBy.fullName", target = "enteredByName")
    ServiceResultResponse toResponse(ServiceResult serviceResult);
}
