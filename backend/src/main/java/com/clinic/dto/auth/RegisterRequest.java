package com.clinic.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    // Strictly require email only
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // Strictly require password only (minimum 6 characters)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
}