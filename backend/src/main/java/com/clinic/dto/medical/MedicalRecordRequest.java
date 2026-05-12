package com.clinic.dto.medical;

import com.clinic.common.enums.MedicalRecordStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedicalRecordRequest {
    @NotNull
    private Integer patientId;
    private Integer appointmentId;
    @NotNull
    private Integer mainDoctorId;
    
    private String diagnosis;
    private String treatment;
    private String note;
    private MedicalRecordStatus status;
    
    // Thêm lưu vết sửa bệnh án
    private Integer updatedByDoctorId;
    private String editReason;
}