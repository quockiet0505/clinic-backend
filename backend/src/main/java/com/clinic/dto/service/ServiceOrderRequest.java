package com.clinic.dto.service;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceOrderRequest {
    @NotNull(message = "Record ID is required")
    private Integer recordId;

    @NotNull(message = "Service ID is required")
    private Integer serviceId;

    @NotNull(message = "Ordered By (Doctor ID) is required")
    private Integer orderedById;
}