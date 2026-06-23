package com.clinic.dto.medical;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TriageRequest {
    private Integer height;
    private BigDecimal weight;
    private String bloodPressure;
    private Integer pulse;
    private String bloodType;
    private String allergies;
    private String chronicDiseases;
    private Double temperature;
}
