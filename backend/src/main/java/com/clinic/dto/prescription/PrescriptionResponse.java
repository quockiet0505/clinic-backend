package com.clinic.dto.prescription;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal; 
import lombok.Data;

@Data
public class PrescriptionResponse {
    private Integer prescriptionId;
    private Integer recordId;
    private Integer patientId;
    private String patientName;
    private String doctorName;
    private String diagnosis;
    private BigDecimal consultationFinalFee;
    private String status;
    private LocalDateTime createdAt;
    private List<PrescriptionItemResponse> items;
}