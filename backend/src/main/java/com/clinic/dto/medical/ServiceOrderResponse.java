package com.clinic.dto.medical;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrderResponse {
    private Integer orderId;
    private Integer recordId;              
    private Integer serviceId;
    private String serviceName;
    private String status;
    private Integer orderedById;            
    private String orderedByName;          
    private Integer sampleCollectedById;    
    private String sampleCollectedByName;  
    private LocalDateTime createdAt;
    private ServiceResultResponse result;  
}