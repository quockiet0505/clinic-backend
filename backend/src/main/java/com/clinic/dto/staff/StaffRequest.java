package com.clinic.dto.staff;

import java.time.LocalDate;

import com.clinic.common.enums.StaffType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StaffRequest {
    @Email(message = "Invalid email format")
    private String email;

    private String password; 

    private Integer expertiseId; 
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    private String gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    
    @NotNull(message = "Staff type is required")
    private StaffType staffType;
    
    private String experience;
    private String specialtyTreatment;
    private String imageUrl;
    private Boolean isFeatured;
    private Integer featuredPriority;
}