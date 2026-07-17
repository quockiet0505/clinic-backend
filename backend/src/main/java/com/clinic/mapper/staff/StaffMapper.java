package com.clinic.mapper.staff;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.staff.StaffRequest;
import com.clinic.dto.staff.StaffResponse;
import com.clinic.entity.staff.Staff;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StaffMapper {
    
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "expertise", ignore = true)
    Staff toEntity(StaffRequest request);

    @Mapping(source = "account.email", target = "email")
    @Mapping(source = "account.isActive", target = "isActive")
    @Mapping(source = "account.accountId", target = "accountId")
    @Mapping(source = "expertise.expertiseId", target = "expertiseId")
    @Mapping(source = "expertise.expertiseName", target = "expertiseName")
    StaffResponse toResponse(Staff staff);
}