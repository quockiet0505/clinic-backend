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
        log.info("Cleaning up database tables to prepare for seeding...");
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        String[] tables = {
            "appointment", "medical_record", "medical_record_vital", "service_order",
            "service_result", "invoice_item", "invoice", "prescription_item",
            "prescription", "doctor_review", "feedback", "follow_up",
            "patient_vital_profile", "device_token", "leave_request", "staff_schedule",
            "doctor_service_price", "patient", "staff", "account_role", "account",
            "role", "expertise", "service", "medicine"
        };
        for (String table : tables) {
            try {
                entityManager.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
            } catch (Exception e) {
                log.warn("Failed to truncate table {}: {}", table, e.getMessage());
            }
        }
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        log.info("Database cleaned up successfully!");

        // 1. Initialize System Roles (KHÔNG có tiền tố ROLE_)
        seedRoleIfNotFound("ADMIN", "Administrator");
        seedRoleIfNotFound("DOCTOR", "Doctor");
        seedRoleIfNotFound("PATIENT", "Patient");
        seedRoleIfNotFound("RECEPTIONIST", "Receptionist");
        seedRoleIfNotFound("NURSE", "Nurse");
        seedRoleIfNotFound("LAB_TECH", "Lab Technician");

        // 2. Initialize Dummy Accounts for Testing
        seedAccountIfNotFound("admin@clinic.com", "12345678", "ADMIN");
        seedAccountIfNotFound("receptionist@clinic.com", "12345678", "RECEPTIONIST");
        seedAccountIfNotFound("doctor@clinic.com", "12345678", "DOCTOR");
        seedAccountIfNotFound("lab_tech@clinic.com", "12345678", "LAB_TECH");
        seedAccountIfNotFound("nurse@clinic.com", "12345678", "NURSE");
        seedAccountIfNotFound("patient1@clinic.com", "12345678", "PATIENT");
    }

    private void seedAccountIfNotFound(String email, String password, String roleCode) {
        Account account = accountRepository.findByEmail(email).orElse(new Account());
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(password));
        account.setIsActive(1);
        account.setFailedAttempt(0);

        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new RuntimeException(roleCode + " role not found during seeding"));
        account.getRoles().clear();
        account.getRoles().add(role);

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