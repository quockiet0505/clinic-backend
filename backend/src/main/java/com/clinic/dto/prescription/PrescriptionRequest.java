package com.clinic.dto.prescription;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrescriptionRequest {
    @NotNull(message = "Medical Record ID is required")
    private Integer recordId;

    @NotEmpty(message = "Prescription must contain at least one medicine")
    @Valid // This ensures validation rules in PrescriptionItemRequest are triggered
    private List<PrescriptionItemRequest> items;
}