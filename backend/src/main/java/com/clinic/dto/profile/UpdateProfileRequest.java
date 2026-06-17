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
    // email không update ở đây (nếu cần thì thêm, nhưng thường không cho đổi email)
}