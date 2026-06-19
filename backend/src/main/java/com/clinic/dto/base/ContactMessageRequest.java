package com.clinic.dto.base;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactMessageRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private String email;

    private String subject;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;
}
