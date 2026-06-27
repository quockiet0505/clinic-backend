package com.clinic.dto.medical;

import java.math.BigDecimal;

import com.clinic.common.enums.ServiceType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceRequest {
    
    @NotBlank(message = "Service name is required")
    private String serviceName;

    @NotNull(message = "Service type is required (e.g., EXAM, LAB_TEST, X_RAY, ULTRASOUND, CT_SCAN, MRI, ENDOSCOPY, OTHER)")
    private ServiceType serviceType;

    private Integer estimatedDuration;

    @NotNull(message = "Original price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal originalPrice;

    private BigDecimal discountPrice;

    private String description;

    private String imageUrl;

    private Boolean isFeatured;

    private Integer featuredPriority;
}