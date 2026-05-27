package com.clinic.dto.medical;

import java.math.BigDecimal;

import com.clinic.common.enums.ServiceType;

import lombok.Data;

@Data
public class ServiceResponse {

    private Integer serviceId;

    private String serviceName;

    private ServiceType serviceType;

    private BigDecimal price;

    private BigDecimal discountPrice;

    private String imageUrl;

    private ServiceResultResponse result;
}