package com.clinic.dto.profile;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    private Integer staffId;
    private Integer accountId;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String email;
    private String address;
    private String roleName;
    private String avatarUrl;
    private LocalDateTime createdAt;
    
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