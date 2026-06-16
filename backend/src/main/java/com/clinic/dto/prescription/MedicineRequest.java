package com.clinic.dto.prescription;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedicineRequest {
    private String name;
    private String activeElement;
    private String packingStandard;
    private String baseUnit;
    private String usageNote;
}