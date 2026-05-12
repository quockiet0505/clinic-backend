package com.clinic.dto.prescription;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PrescriptionItemResponse {
    private Integer medicineId;
    private String medicineName; 
    
    private String unit;
    private BigDecimal quantity;
    private String dosage;
    private BigDecimal price;
}