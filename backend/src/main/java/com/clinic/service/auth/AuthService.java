package com.clinic.service.auth;

import com.clinic.dto.auth.AuthResponse;
import com.clinic.dto.auth.LoginRequest;
import com.clinic.dto.auth.RegisterRequest;
import com.clinic.entity.auth.Account;
import com.clinic.entity.auth.Role;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.auth.RoleRepository;
import com.clinic.security.CustomUserDetails;
import com.clinic.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse registerPatient(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered.");
        }

        // Only email and password as requested
        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        
        Role role = roleRepository.findByRoleCode("ROLE_PATIENT")
                .orElseThrow(() -> new RuntimeException("ROLE_PATIENT role not found."));
        account.getRoles().add(role);

        accountRepository.save(account);

        CustomUserDetails userDetails = new CustomUserDetails(account);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .accountId(account.getAccountId())
                .email(account.getEmail())
                .token(token)
                .roles(List.of(role.getRoleCode()))
                .build();
    }

    public AuthResponse loginPatient(LoginRequest request) {
        AuthResponse response = processAuthentication(request);
        
        // Ensure only patients can log in here
        if (!response.getRoles().contains("ROLE_PATIENT")) {
            throw new RuntimeException("Access Denied: Not a patient account.");
        }
        return response;
    }

    public AuthResponse loginStaff(LoginRequest request) {
        AuthResponse response = processAuthentication(request);
        
        // Ensure patients cannot access the staff portal
        boolean isStaff = response.getRoles().stream()
                .anyMatch(role -> role.equals("ROLE_ADMIN") || 
                                  role.equals("ROLE_DOCTOR") || 
                                  role.equals("ROLE_STAFF") ||
                                  role.equals("ROLE_LAB_TECH"));
                                  
        if (!isStaff) {
            throw new RuntimeException("Access Denied: Staff privileges required.");
        }
        
        return response;
    }

    private AuthResponse processAuthentication(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Account account = userDetails.getAccount();
        
        if (account.getIsActive() == 0) {
            throw new RuntimeException("Account is deactivated.");
        }

        // Reset failed attempts upon successful login
        if (account.getFailedAttempt() > 0) {
            account.setFailedAttempt(0);
            accountRepository.save(account);
        }

        String token = jwtService.generateToken(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .accountId(account.getAccountId())
                .email(account.getEmail())
                .token(token)
                .roles(roles)
                .build();
    }
}