package com.clinic.dto.patient;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class PatientResponse {
    private Integer patientId;
    private Integer accountId;
    private String email;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private String avatarUrl;
    private Integer isActive;
    
    // Vitals data
    private Integer height;
    private BigDecimal weight;
    private String bloodPressure;
    private Integer pulse;
    private String bloodType;
    private String allergies;
    private String medicalHistory;
    private Boolean bookingLocked;
    private Integer cancelSpamCount;
}