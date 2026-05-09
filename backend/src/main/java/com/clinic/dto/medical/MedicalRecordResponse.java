package com.clinic.dto.medical;
import java.time.LocalDateTime;

import com.clinic.common.enums.MedicalRecordStatus;

import lombok.Data;

@Data
public class MedicalRecordResponse {
    private Integer recordId;
    private Integer patientId;
    private String patientName;
    private Integer appointmentId;
    private Integer mainDoctorId;
    private String doctorName;
    private String diagnosis;
    private String treatment;
    private String note;
    private MedicalRecordStatus status;
    private LocalDateTime createdAt;
}