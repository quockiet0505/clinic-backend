package com.clinic.dto.prescription;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrescriptionItemRequest {
    private Integer medicineId;
    
    @NotNull
    private String medicineName;
    
    @NotNull
    private String unit;
    
    @NotNull
    private BigDecimal quantity; 
    
    @jakarta.validation.constraints.NotBlank(message = "Liều lượng không được để trống")
    private String dosage;

    @jakarta.validation.constraints.NotBlank(message = "Tần suất không được để trống")
    private String frequency;

    @NotNull(message = "Số ngày không được để trống")
    @jakarta.validation.constraints.Min(value = 1, message = "Số ngày phải lớn hơn 0")
    private Integer durationDays;
}