package com.clinic.dto.service;
import java.time.LocalDateTime;

import com.clinic.common.enums.ServiceOrderStatus;

import lombok.Data;

@Data
public class ServiceOrderResponse {
    private Integer orderId;
    private Integer recordId;
    private Integer serviceId;
    private String serviceName;
    private Integer orderedById;
    private String orderedByName;
    private ServiceOrderStatus status;
    private LocalDateTime createdAt;
}