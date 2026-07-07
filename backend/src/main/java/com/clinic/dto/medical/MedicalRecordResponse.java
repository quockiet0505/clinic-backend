package com.clinic.dto.medical;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.MedicalRecordStatus;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MedicalRecordResponse {
    private Integer recordId;
    private Integer patientId;
    private String patientName;
    private Integer appointmentId;
    private AppointmentStatus appointmentStatus;
    private Integer queueNumber;
    private LocalDateTime checkinTime;
    private Integer mainDoctorId;
    private String mainDoctorName;
    private String diagnosis;
    private String treatment;
    private String note;
    private MedicalRecordStatus status;
    private LocalDateTime createdAt;
    
    private java.math.BigDecimal consultationFinalFee;
    
    // Thêm lưu vết
    private Integer updatedByDoctorId;
    private String updatedByDoctorName;
    private String editReason;
    
    private Boolean vitalsTaken;
}