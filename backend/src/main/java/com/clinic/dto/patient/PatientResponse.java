package com.clinic.dto.patient;

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
    
    // Vitals data
    private Integer height;
    private String bloodType;
    private String allergies;
    private String medicalHistory;
}