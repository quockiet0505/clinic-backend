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
    private String patientFullName;
    private String patientGender;
    private java.time.LocalDate patientDob;
    private String patientPhone;
    private String patientAddress;
    private Integer appointmentId;
    private String appointmentStatus;
    private Integer queueNumber;
    private Integer mainDoctorId;
    private String mainDoctorName;
    private String diagnosis;
    private String treatment;
    private String note;
    private String status;
    private Boolean vitalsTaken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private java.math.BigDecimal consultationFee;
    private java.math.BigDecimal serviceFee;
    
    private PrescriptionResponse prescription;
    private List<ServiceOrderResponse> serviceOrders;
    private List<FollowUpResponse> followUps;
}