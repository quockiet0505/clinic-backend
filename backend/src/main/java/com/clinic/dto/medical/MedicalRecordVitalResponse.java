package com.clinic.dto.medical;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class MedicalRecordVitalResponse {
    private Integer recordId;
    private BigDecimal weight;
    private String bloodPressure;
    private Integer pulse;
    private Integer recordedById;
    private String recordedByName;
}