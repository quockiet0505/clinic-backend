package com.clinic.dto.medical;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedicalRecordRequest {
    @NotNull(message = "Patient ID is required")
    private Integer patientId;

    private Integer appointmentId; // Nullable for direct walk-ins

    @NotNull(message = "Doctor ID is required")
    private Integer mainDoctorId;

    private String diagnosis;
    private String treatment;
    private String note;
}