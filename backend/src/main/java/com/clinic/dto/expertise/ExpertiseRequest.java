package com.clinic.dto.expertise;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExpertiseRequest {
    @NotBlank(message = "Expertise name is required")
    private String expertiseName;
}