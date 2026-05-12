package com.clinic.dto.prescription;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class MedicineResponse {
    private Integer medicineId;
    private String name;
    private String activeElement;
    
    private String packingStandard;
    private String baseUnit;
    
    private BigDecimal sellPrice;
    private String usageNote;
}