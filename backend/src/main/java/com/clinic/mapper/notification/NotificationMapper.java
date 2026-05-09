package com.clinic.mapper.notification;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.notification.NotificationResponse;
import com.clinic.entity.notification.Notification;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mapping(source = "account.accountId", target = "accountId")
    NotificationResponse toResponse(Notification notification);
}