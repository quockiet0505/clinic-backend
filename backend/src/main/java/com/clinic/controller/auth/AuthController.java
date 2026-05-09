package com.clinic.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController; 

import com.clinic.dto.auth.AuthResponse;
import com.clinic.dto.auth.LoginRequest;
import com.clinic.dto.auth.RegisterRequest;
import com.clinic.service.auth.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //  PATIENT ENDPOINTS 

    @PostMapping("/patient/register")
    public ResponseEntity<AuthResponse> registerPatient(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerPatient(request));
    }

    @PostMapping("/patient/login")
    public ResponseEntity<AuthResponse> loginPatient(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.loginPatient(request));
    }

    //  STAFF/ADMIN ENDPOINTS 

    @PostMapping("/staff/login")
    public ResponseEntity<AuthResponse> loginStaff(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.loginStaff(request));
    }
}