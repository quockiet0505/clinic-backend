package com.clinic.controller.auth;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.auth.AuthResponse;
import com.clinic.dto.auth.LoginRequest;
import com.clinic.dto.auth.RegisterRequest;
import com.clinic.entity.auth.Account;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.security.JwtService;
import com.clinic.service.auth.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtService jwtService;
    private final AccountRepository accountRepository;

    @PostMapping("/patient/register")
    public ResponseEntity<?> registerPatient(@RequestBody RegisterRequest request, HttpServletResponse response) {
        try {
            AuthResponse res = authService.registerPatient(request, response);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/patient/login")
    public ResponseEntity<?> loginPatient(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            AuthResponse res = authService.loginPatient(request, response);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/staff/login")
    public ResponseEntity<?> loginStaff(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            AuthResponse res = authService.loginStaff(request, response);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Staff login error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            String token = extractTokenFromCookie(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No token found"));
            }
            String email = jwtService.extractEmail(token);
            Account account = accountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<String> roles = account.getRoles().stream()
                    .map(role -> role.getRoleCode())
                    .collect(Collectors.toList());
            Map<String, Object> userData = Map.of(
                    "accountId", account.getAccountId(),
                    "email", account.getEmail(),
                    "roles", roles
            );
            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            log.error("Get current user error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
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