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
    private LocalDateTime createdAt;
}