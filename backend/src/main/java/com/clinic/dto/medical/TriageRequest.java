package com.clinic.dto.medical;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TriageRequest {
    @NotNull(message = "Chiều cao không được để trống")
    private Integer height;

    @NotNull(message = "Cân nặng không được để trống")
    private BigDecimal weight;

    @NotBlank(message = "Huyết áp không được để trống")
    private String bloodPressure;

    @NotNull(message = "Nhịp tim không được để trống")
    private Integer pulse;

    private String bloodType;
    private String allergies;
    private String chronicDiseases;

    @NotNull(message = "Nhiệt độ không được để trống")
    private Double temperature;
}
