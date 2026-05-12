package com.clinic.mapper.staff;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import com.clinic.dto.staff.LeaveRequestRequest;
import com.clinic.dto.staff.LeaveRequestResponse;
import com.clinic.entity.staff.LeaveRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LeaveRequestMapper {

    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    LeaveRequest toEntity(LeaveRequestRequest request);

    @Mapping(source = "staff.staffId", target = "staffId")
    @Mapping(source = "staff.fullName", target = "staffName")
    @Mapping(source = "approvedBy.staffId", target = "approvedById")
    @Mapping(source = "approvedBy.fullName", target = "approvedByName")
    LeaveRequestResponse toResponse(LeaveRequest leaveRequest);
}