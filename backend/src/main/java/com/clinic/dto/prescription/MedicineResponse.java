package com.clinic.dto.prescription;

import lombok.Data;

@Data
public class MedicineResponse {
    private Integer medicineId;
    private String name;
    private String activeElement;
    private String packingStandard;
    private String baseUnit;
    private String usageNote;
}