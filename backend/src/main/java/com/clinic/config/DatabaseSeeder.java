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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Initialize System Roles
        seedRoleIfNotFound("ROLE_ADMIN", "Administrator");
        seedRoleIfNotFound("ROLE_DOCTOR", "Doctor");
        seedRoleIfNotFound("ROLE_STAFF", "Clinic Staff");
        seedRoleIfNotFound("ROLE_PATIENT", "Patient");

        // 2. Initialize Master Admin Account
        String adminEmail = "admin@gmail.com";
        if (!accountRepository.existsByEmail(adminEmail)) {
            log.info("No admin account found. Seeding default Master Admin...");

            Account admin = new Account();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("Trustcare@2026"));
            admin.setIsActive(1);
            admin.setFailedAttempt(0);

            // Fetch the admin role and assign it
            Role adminRole = roleRepository.findByRoleCode("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found during seeding"));
            
            admin.getRoles().add(adminRole);

            accountRepository.save(admin);
            log.info(" Master Admin account created successfully! Email: {} | Password: Trustcare@2026", adminEmail);
        } else {
            log.info("Database already contains the admin account. Skipping seeder.");
        }
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