package com.clinic.dto.prescription;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrescriptionItemRequest {
    private Integer medicineId;
    
    @NotNull
    private String medicineName;
    
    @NotNull
    private String unit;
    
    @NotNull
    private BigDecimal quantity; 
    
    private String dosage;
}