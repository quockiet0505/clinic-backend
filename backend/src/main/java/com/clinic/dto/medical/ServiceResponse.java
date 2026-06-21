package com.clinic.dto.medical;

import java.math.BigDecimal;

import com.clinic.common.enums.ServiceType;

import lombok.Data;

@Data
public class ServiceResponse {

    private Integer serviceId;

    private String serviceName;

    private ServiceType serviceType;

    private BigDecimal originalPrice;

    private BigDecimal discountPrice;

    private String description;

    private String imageUrl;

    private Boolean isFeatured;

    private Integer featuredPriority;

    private ServiceResultResponse result;
}