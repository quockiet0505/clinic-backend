package com.clinic.dto.medical;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceResultRequest {
    @NotNull
    private Integer orderId;
    private String resultData;
    private String conclusion;
    private String attachmentUrl; 
    @NotNull
    private Integer enteredById;
}