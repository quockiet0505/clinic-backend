package com.clinic.dto.service;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceResultRequest {
    @NotNull(message = "Order ID is required")
    private Integer orderId;

    @NotBlank(message = "Result data cannot be empty")
    private String resultData;

    @NotBlank(message = "Conclusion cannot be empty")
    private String conclusion;

    @NotNull(message = "Entered By (Lab Tech ID) is required")
    private Integer enteredById;
}