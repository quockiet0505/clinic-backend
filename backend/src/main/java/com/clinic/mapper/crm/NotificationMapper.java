package com.clinic.mapper.crm;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.crm.NotificationResponse;
import com.clinic.entity.crm.Notification;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mapping(source = "account.accountId", target = "accountId")
    NotificationResponse toResponse(Notification notification);
}