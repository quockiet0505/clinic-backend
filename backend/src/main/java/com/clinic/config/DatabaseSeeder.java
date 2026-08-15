package com.clinic.config;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.entity.auth.Account;
import com.clinic.entity.auth.Role;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.auth.RoleRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.annotation.Order;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class DatabaseSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {


        // 1. Initialize System Roles (KHÔNG có tiền tố ROLE_)
        seedRoleIfNotFound("ADMIN", "Administrator");
        seedRoleIfNotFound("DOCTOR", "Doctor");
        // seedRoleIfNotFound("STAFF", "Clinic Staff");
        seedRoleIfNotFound("PATIENT", "Patient");
        seedRoleIfNotFound("RECEPTIONIST", "Receptionist");
        seedRoleIfNotFound("NURSE", "Nurse");
        seedRoleIfNotFound("LAB_TECH", "Lab Technician");

        // 2. Initialize Master Admin Account
        String adminEmail = "kiet@gmail.com";
        seedAccountIfNotFound(adminEmail, "12345678", "ADMIN");

        // 3. Initialize Dummy Accounts for Testing
        seedAccountIfNotFound("admin@clinic.com", "12345678", "ADMIN");
        seedAccountIfNotFound("receptionist@clinic.com", "12345678", "RECEPTIONIST");
        seedAccountIfNotFound("doctor@clinic.com", "12345678", "DOCTOR");
        seedAccountIfNotFound("lab_tech@clinic.com", "12345678", "LAB_TECH");
        seedAccountIfNotFound("patient1@clinic.com", "12345678", "PATIENT");

    }

    private void seedAccountIfNotFound(String email, String password, String roleCode) {
        Account account = accountRepository.findByEmail(email).orElse(new Account());
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(password));
        account.setIsActive(1);
        account.setFailedAttempt(0);

        if (account.getRoles() == null || account.getRoles().isEmpty()) {
            Role role = roleRepository.findByRoleCode(roleCode)
                    .orElseThrow(() -> new RuntimeException(roleCode + " role not found during seeding"));
            account.getRoles().add(role);
        }

        accountRepository.save(account);
        log.info("Created/Updated test account: {} with role: {}", email, roleCode);
    }

    private void seedRoleIfNotFound(String roleCode, String roleName) {
        Optional<Role> existingRole = roleRepository.findByRoleCode(roleCode);
        if (existingRole.isEmpty()) {
            Role newRole = new Role();
            newRole.setRoleCode(roleCode);
            newRole.setRoleName(roleName);
            roleRepository.save(newRole);
            log.info("Created missing role: {}", roleCode);
        }
    }
}