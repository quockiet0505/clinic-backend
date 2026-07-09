package com.clinic.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleRegisterRequest extends RegisterRequest {
    @NotBlank(message = "Google ID Token is required")
    private String idToken;
}
