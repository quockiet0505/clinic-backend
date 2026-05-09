package com.clinic.dto.prescription;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class PrescriptionResponse {
    private Integer prescriptionId;
    private Integer recordId;
    private LocalDateTime createdAt;
    private List<PrescriptionItemResponse> items;
}