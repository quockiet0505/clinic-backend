package com.clinic.dto.staff;

import java.time.LocalDate;

import com.clinic.common.enums.StaffType;

import lombok.Data;

@Data
public class StaffResponse {
    private Integer staffId;
    private String email;
    private Integer expertiseId;
    private String expertiseName;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private StaffType staffType;
    private String experience;
    private String imageUrl;
    private Integer isDeleted;
}