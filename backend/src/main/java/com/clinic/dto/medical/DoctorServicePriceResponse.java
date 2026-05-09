package com.clinic.dto.medical;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DoctorServicePriceResponse {
    private Integer id;
    private Integer staffId;
    private String doctorName;
    private Integer serviceId;
    private String serviceName;
    private BigDecimal price;
}