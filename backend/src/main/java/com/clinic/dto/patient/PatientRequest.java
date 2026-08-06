package com.clinic.dto.patient;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PatientRequest {
    private Integer accountId; 
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    private String gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private String avatarUrl;
    
    // Vitals 
    private Integer height;
    private BigDecimal weight;
    private String bloodPressure;
    private Integer pulse;
    private String bloodType;
    private String allergies;
    private String medicalHistory;
}