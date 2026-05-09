package com.clinic.mapper.expertise;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.expertise.ExpertiseRequest;
import com.clinic.dto.expertise.ExpertiseResponse;
import com.clinic.entity.staff.Expertise;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExpertiseMapper {
    Expertise toEntity(ExpertiseRequest request);
    ExpertiseResponse toResponse(Expertise expertise);
}