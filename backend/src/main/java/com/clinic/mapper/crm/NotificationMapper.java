package com.clinic.mapper.crm;

import com.clinic.dto.crm.NotificationResponse;
import com.clinic.entity.crm.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "accountName", source = "account.email")
    @Mapping(target = "accountId", source = "account.accountId")
    NotificationResponse toResponse(Notification notification);
}