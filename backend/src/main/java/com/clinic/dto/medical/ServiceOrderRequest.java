package com.clinic.dto.medical;

import java.time.LocalDateTime;

import com.clinic.common.enums.ServiceOrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceOrderRequest {
    @NotNull
    private Integer recordId;
    @NotNull
    private Integer serviceId;
    @NotNull
    private Integer orderedById;
    
    private ServiceOrderStatus status;
    
    // Cập nhật lấy mẫu / từ chối
    private String rejectionReason;
    private LocalDateTime sampleCollectedAt;
    private Integer sampleCollectedById;
}