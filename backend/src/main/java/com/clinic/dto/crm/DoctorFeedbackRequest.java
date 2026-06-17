package com.clinic.dto.crm;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DoctorFeedbackRequest {
    @NotNull(message = "Doctor ID is required")
    private Integer doctorId;

    @NotNull(message = "Patient ID is required")
    private Integer patientId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    private String comment;
}