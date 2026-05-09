package com.clinic.mapper.staff;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.staff.StaffScheduleRequest;
import com.clinic.dto.staff.StaffScheduleResponse;
import com.clinic.entity.staff.StaffSchedule;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StaffScheduleMapper {
    
    @Mapping(target = "staff", ignore = true)
    StaffSchedule toEntity(StaffScheduleRequest request);

    @Mapping(source = "staff.staffId", target = "staffId")
    @Mapping(source = "staff.fullName", target = "staffName")
    StaffScheduleResponse toResponse(StaffSchedule staffSchedule);
}