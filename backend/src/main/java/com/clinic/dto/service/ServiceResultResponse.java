package com.clinic.dto.service;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ServiceResultResponse {
    private Integer resultId;
    private Integer orderId;
    private String serviceName;
    private String resultData;
    private String conclusion;
    private Integer enteredById;
    private String enteredByName;
    private LocalDateTime enteredAt;
}