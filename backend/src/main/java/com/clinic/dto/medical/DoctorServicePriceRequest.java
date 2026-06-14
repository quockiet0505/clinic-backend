package com.clinic.dto.medical;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DoctorServicePriceRequest {

    @NotNull(message = "Staff ID is required")
    private Integer staffId;

    @NotNull(message = "Service ID is required")
    private Integer serviceId;

    @NotNull(message = "Original price is required")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal originalPrice;

    @NotNull(message = "Discount price is required")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal discountPrice;
}