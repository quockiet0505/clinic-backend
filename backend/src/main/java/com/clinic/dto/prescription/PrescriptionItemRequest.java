package com.clinic.dto.prescription;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrescriptionItemRequest {
    @NotNull
    private Integer medicineId;
    
    @NotNull
    private String unit;
    
    @NotNull
    private BigDecimal quantity; 
    
    private String dosage;
    private BigDecimal price;
}