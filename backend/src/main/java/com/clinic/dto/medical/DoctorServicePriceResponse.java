package com.clinic.dto.medical;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DoctorServicePriceResponse {

    private Integer id;

    private Integer staffId;
    private String doctorName;

    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;

    private String imageUrl;
}