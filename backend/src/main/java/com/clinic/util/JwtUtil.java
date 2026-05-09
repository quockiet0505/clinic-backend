package com.clinic.util;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

// Helper class specifically to extract JWT from incoming HTTP requests
@Component
public class JwtUtil {
    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}