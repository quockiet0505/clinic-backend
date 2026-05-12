package com.clinic.dto.prescription;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedicineRequest {
    @NotBlank
    private String name;
    private String activeElement;
    
    private String packingStandard;
    private String baseUnit;
    
    private BigDecimal sellPrice;
    private String usageNote;
}