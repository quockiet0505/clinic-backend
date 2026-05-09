package com.clinic.dto.medical;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MedicalRecordVitalRequest {
    @NotNull(message = "Record ID is required")
    private Integer recordId;

    @Positive(message = "Weight must be positive")
    private BigDecimal weight;

    private String bloodPressure;
    
    @Positive(message = "Pulse must be positive")
    private Integer pulse;

    @NotNull(message = "Recorded By (Staff ID) is required")
    private Integer recordedById;
}