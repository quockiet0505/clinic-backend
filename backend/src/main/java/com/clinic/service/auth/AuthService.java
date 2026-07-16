package com.clinic.service.auth;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import com.clinic.dto.auth.LoginRequest;
import com.clinic.dto.auth.RegisterRequest;
import com.clinic.dto.auth.GoogleAuthRequest;
import com.clinic.dto.auth.GoogleRegisterRequest;
import com.clinic.dto.auth.AuthResponse;
import com.clinic.exception.RequiresRegistrationException;
import com.clinic.entity.auth.Account;
import com.clinic.entity.auth.Role;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.patient.PatientVitalProfile;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.auth.RoleRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.patient.PatientVitalProfileRepository;
import com.clinic.security.CustomUserDetails;
import com.clinic.security.JwtService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository; 
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final PatientVitalProfileRepository vitalProfileRepository;

    @Transactional
public AuthResponse registerPatient(RegisterRequest request, HttpServletResponse response) {
    log.info("Registering patient with email: {}", request.getEmail());
    
    if (accountRepository.existsByEmail(request.getEmail())) {
        log.warn("Email already registered: {}", request.getEmail());
        throw new RuntimeException("Email is already in use. Please login or use a different email.");
    }
    
    Account account = new Account();
    account.setEmail(request.getEmail());
    account.setPassword(passwordEncoder.encode(request.getPassword()));
    account.setIsActive(1);
    
    Role role = roleRepository.findByRoleCode("PATIENT")
            .orElseThrow(() -> new RuntimeException("PATIENT role not found."));
    account.getRoles().add(role);
    
    account = accountRepository.save(account);

    // Tạo Patient 
    Patient patient = new Patient();
    patient.setAccount(account);
    patient.setFullName(request.getFullName());
    patient.setPhone(request.getPhone());
    patient.setGender(request.getGender());
    patient.setDateOfBirth(request.getDateOfBirth());
    patient.setAddress(request.getAddress());
    patient.setIsDeleted(0);
    Patient savedPatient = patientRepository.save(patient);

    // Tạo PatientVitalProfile rỗng
    PatientVitalProfile vitalProfile = new PatientVitalProfile();
    vitalProfile.setPatient(savedPatient);
    vitalProfileRepository.save(vitalProfile);

    CustomUserDetails userDetails = new CustomUserDetails(account);
    String token = jwtService.generateToken(userDetails);
    
    setCookie(response, token);
    
    log.info("Registered successfully, token generated for {}", request.getEmail());

    return AuthResponse.builder()
            .accountId(account.getAccountId())
            .email(account.getEmail())
            .token(token)
            .roles(List.of(role.getRoleCode()))
            .build();
}

    @Transactional
    public AuthResponse loginPatient(LoginRequest request, HttpServletResponse response) {
        log.info("Login attempt for patient: {}", request.getEmail());

       
        try {
            AuthResponse authRes = processAuthentication(request);
            if (!authRes.getRoles().contains("ROLE_PATIENT")) {
                log.warn("Account {} is not patient", request.getEmail());

                log.warn("Roles after login = {}", authRes.getRoles());
                
                throw new RuntimeException("Access Denied: Not a patient account.");
            }
            setCookie(response, authRes.getToken());
            log.info("Patient login successful: {}", request.getEmail());
            

            return authRes;
        } catch (BadCredentialsException e) {
            log.error("Bad credentials for email: {}", request.getEmail());
            throw new RuntimeException("Invalid email or password.");
        } catch (Exception e) {
            log.error("Login failed for {}: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public AuthResponse loginStaff(LoginRequest request, HttpServletResponse response) {
        log.info("Login attempt for staff: {}", request.getEmail());
        try {
            AuthResponse authRes = processAuthentication(request);
            boolean isStaff = authRes.getRoles().stream()
                .anyMatch(role ->
                        role.equals("ROLE_ADMIN") ||
                        role.equals("ROLE_DOCTOR") ||
                        role.equals("ROLE_LAB_TECH") ||
                        role.equals("ROLE_RECEPTIONIST") ||
                        role.equals("ROLE_NURSE"));
            if (!isStaff) {
                log.warn("Account {} is not staff", request.getEmail());
                throw new RuntimeException("Access Denied: Staff privileges required.");
            }
            setCookie(response, authRes.getToken());
            log.info("Staff login successful: {}", request.getEmail());
            return authRes;
        } catch (BadCredentialsException e) {
            log.error("Bad credentials for staff: {}", request.getEmail());
            throw new RuntimeException("Invalid email or password.");
        } catch (Exception e) {
            log.error("Staff login failed: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public AuthResponse googleLogin(GoogleAuthRequest request, HttpServletResponse response) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(request.getIdToken());
            String email = decodedToken.getEmail();
            String name = decodedToken.getName();
            String picture = decodedToken.getPicture();

            if (email == null) {
                throw new RuntimeException("Email not found in Google token");
            }

            return accountRepository.findByEmail(email).map(account -> {
                // User exists, login
                CustomUserDetails userDetails = new CustomUserDetails(account);
                String token = jwtService.generateToken(userDetails);
                setCookie(response, token);
                
                List<String> roles = userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());

                return AuthResponse.builder()
                        .accountId(account.getAccountId())
                        .email(account.getEmail())
                        .token(token)
                        .roles(roles)
                        .build();
            }).orElseThrow(() -> new RequiresRegistrationException(email, name, picture));

        } catch (RequiresRegistrationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google login failed: {}", e.getMessage());
            throw new RuntimeException("Invalid Google Token");
        }
    }

    @Transactional
    public AuthResponse googleRegister(GoogleRegisterRequest request, HttpServletResponse response) {
        try {
            // Verify token again to ensure it's legit
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(request.getIdToken());
            String email = decodedToken.getEmail();

            if (email == null || !email.equals(request.getEmail())) {
                throw new RuntimeException("Email mismatch or not found in Google token");
            }

            // Register using existing logic but mock a random password since it's Google Auth
            request.setPassword(java.util.UUID.randomUUID().toString() + "Gg@1");
            return registerPatient(request, response);

        } catch (Exception e) {
            log.error("Google register failed: {}", e.getMessage());
            throw new RuntimeException("Google registration failed: " + e.getMessage());
        }
    }

    private AuthResponse processAuthentication(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Account account = userDetails.getAccount();
        
        if (account.getIsActive() == null || account.getIsActive() == 0) {
            throw new RuntimeException("Account is deactivated.");
        }
        
        if (account.getFailedAttempt() != null && account.getFailedAttempt() > 0) {
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

    private void setCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(false) 
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    
}