package com.clinic.dto.medical;

import java.time.LocalDateTime;
import java.util.List;

import com.clinic.dto.crm.FollowUpResponse;
import com.clinic.dto.prescription.PrescriptionResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordDetailResponse {
    private Integer recordId;
    private Integer patientId;
    private Integer appointmentId;
    private Integer mainDoctorId;
    private String doctorName;
    private String diagnosis;
    private String treatment;
    private String note;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private PrescriptionResponse prescription;
    private List<ServiceOrderResponse> serviceOrders;
    private List<FollowUpResponse> followUps;
}