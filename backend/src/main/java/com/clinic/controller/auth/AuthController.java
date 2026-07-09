package com.clinic.controller.auth;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.auth.AuthResponse;
import com.clinic.dto.auth.LoginRequest;
import com.clinic.dto.auth.RegisterRequest;
import com.clinic.dto.auth.GoogleAuthRequest;
import com.clinic.dto.auth.GoogleRegisterRequest;
import com.clinic.dto.common.ApiResponse;
import com.clinic.exception.RequiresRegistrationException;
import com.clinic.entity.auth.Account;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.security.JwtService;
import com.clinic.service.auth.AuthService;
import com.clinic.util.ResponseUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log =
            LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    private final JwtService jwtService;

    private final AccountRepository accountRepository;

    @PostMapping("/patient/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerPatient(
            @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {

        AuthResponse res =
                authService.registerPatient(request, response);

        return ResponseUtil.success(
                "Patient registered successfully",
                res
        );
    }

    @PostMapping("/patient/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginPatient(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {

        AuthResponse res =
                authService.loginPatient(request, response);

        return ResponseUtil.success(
                "Patient login successful",
                res
        );
    }

    @PostMapping("/staff/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginStaff(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {

        AuthResponse res =
                authService.loginStaff(request, response);

        return ResponseUtil.success(
                "Staff login successful",
                res
        );
    }

    @PostMapping("/google/login")
    public ResponseEntity<ApiResponse<Object>> googleLogin(
            @RequestBody GoogleAuthRequest request,
            HttpServletResponse response
    ) {
        try {
            AuthResponse res = authService.googleLogin(request, response);
            return ResponseUtil.success("Google login successful", res);
        } catch (RequiresRegistrationException e) {
            return ResponseEntity.status(404).body(ApiResponse.<Object>builder()
                    .success(false)
                    .message("REQUIRES_REGISTRATION")
                    .data(Map.of(
                            "email", e.getEmail(),
                            "name", e.getName() != null ? e.getName() : "",
                            "picture", e.getPicture() != null ? e.getPicture() : ""
                    ))
                    .build());
        }
    }

    @PostMapping("/google/register")
    public ResponseEntity<ApiResponse<AuthResponse>> googleRegister(
            @RequestBody GoogleRegisterRequest request,
            HttpServletResponse response
    ) {
        AuthResponse res = authService.googleRegister(request, response);
        return ResponseUtil.success("Google registration successful", res);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            HttpServletRequest request
    ) {

        String token = extractTokenFromCookie(request);

        if (token == null) {
            throw new RuntimeException("No token found");
        }

        String email = jwtService.extractEmail(token);

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<String> roles = account.getRoles()
                .stream()
                .map(role -> role.getRoleCode())
                .collect(Collectors.toList());

        Map<String, Object> userData = Map.of(
                "accountId", account.getAccountId(),
                "email", account.getEmail(),
                "roles", roles
        );

        return ResponseUtil.success(
                "Current user retrieved successfully",
                userData
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(
            HttpServletResponse response
    ) {

        Cookie cookie = new Cookie("token", null);

        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseUtil.success(
                "Logout successful",
                null
        );
    }

    private String extractTokenFromCookie(
            HttpServletRequest request
    ) {

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {

            for (Cookie cookie : cookies) {

                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

   
}