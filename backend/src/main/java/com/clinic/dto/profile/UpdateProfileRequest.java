package com.clinic.dto.profile;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private String avatarUrl;
    
    // Medical fields
    private Integer height;
    private java.math.BigDecimal weight;
    private String bloodPressure;
    private Integer pulse;
    private String bloodType;
    private String allergies;
    private String chronicDiseases;
    private String medicalHistory;
}