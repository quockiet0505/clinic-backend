package com.clinic.mapper.staff;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.staff.ExpertiseRequest;
import com.clinic.dto.staff.ExpertiseResponse;
import com.clinic.entity.staff.Expertise;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExpertiseMapper {
    Expertise toEntity(ExpertiseRequest request);
    ExpertiseResponse toResponse(Expertise expertise);
}