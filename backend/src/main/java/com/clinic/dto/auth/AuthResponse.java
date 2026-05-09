package com.clinic.dto.auth;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private Integer accountId;
    private String email;
    private String token;
    private List<String> roles;
}