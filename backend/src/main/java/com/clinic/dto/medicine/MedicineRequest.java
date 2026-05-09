package com.clinic.dto.medicine;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedicineRequest {
    
    @NotBlank(message = "Medicine name is required")
    private String name;

    @NotBlank(message = "Unit is required (e.g., Pill, Bottle, Tube)")
    private String unit;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    private String usageNote;
    private String activeElement;
    private String productionUnit;
    
    @NotNull(message = "Manufacturing date (mfg) is required")
    private LocalDate mfg;
    
    @NotNull(message = "Expiration date (exp) is required")
    private LocalDate exp;
}