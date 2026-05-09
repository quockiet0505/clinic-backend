package com.clinic.dto.prescription;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrescriptionItemRequest {
    @NotNull(message = "Medicine ID is required")
    private Integer medicineId;

    @NotBlank(message = "Dosage instruction is required")
    private String dosage;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}