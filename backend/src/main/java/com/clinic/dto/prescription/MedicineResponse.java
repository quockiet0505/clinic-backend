package com.clinic.dto.prescription;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class MedicineResponse {
    private Integer medicineId;
    private String name;
    private String unit;
    private BigDecimal price;
    private Integer quantity;
    private String usageNote;
    private String activeElement;
    private String productionUnit;
    private LocalDate mfg;
    private LocalDate exp;
}