package com.clinic.dto.medical;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    private String customServiceName;
    private String doctorNote;
    private String patientName;
    private String doctorName;
    private LocalDate appointmentDate;
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private String status;
    private Integer orderedById;            
    private String orderedByName;          
    private Integer sampleCollectedById;    
    private String sampleCollectedByName;  
    private LocalDateTime createdAt;
    private java.math.BigDecimal serviceOriginalFee;
    private java.math.BigDecimal serviceDiscount;
    private java.math.BigDecimal serviceFinalFee;
    private ServiceResultResponse result;  
}