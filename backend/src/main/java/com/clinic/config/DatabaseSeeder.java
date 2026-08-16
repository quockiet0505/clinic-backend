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
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DatabaseSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Cleaning up transaction tables to prepare for seeding...");
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        // Delete quockietdev account forcibly from DB
        try {
            entityManager.createNativeQuery("DELETE FROM account_role WHERE account_id IN (SELECT account_id FROM account WHERE email='quockietdev@gmail.com')").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM account WHERE email='quockietdev@gmail.com'").executeUpdate();
        } catch(Exception e) {
            log.warn("Failed to delete quockietdev: " + e.getMessage());
        }

        String[] tables = {
            "appointment", "medical_record", "medical_record_vital", "service_order",
            "service_result", "invoice_item", "invoice", "prescription_item",
            "prescription", "doctor_review", "feedback", "follow_up",
            "device_token", "leave_request", "staff_schedule", "notification",
            "patient_vital_profile", "patient"
        };
        for (String table : tables) {
            try {
                entityManager.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
            } catch (Exception e) {
                log.warn("Failed to truncate table {}: {}", table, e.getMessage());
            }
        }
        // Delete patient accounts to avoid orphans
        try {
            entityManager.createNativeQuery("DELETE FROM account WHERE account_id NOT IN (SELECT account_id FROM staff WHERE account_id IS NOT NULL) AND email NOT IN ('admin@clinic.com', 'receptionist@clinic.com', 'doctor@clinic.com', 'lab_tech@clinic.com', 'nurse@clinic.com', 'kiet@gmail.com', 'quockietdev@gmail.com')").executeUpdate();
        } catch (Exception e) {
            log.warn("Failed to clean up patient accounts: {}", e.getMessage());
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

        // 2. Initialize Master Admin Account
        seedAccountIfNotFound("kiet@gmail.com", "12345678", "ADMIN");

        // 3. Initialize Dummy Accounts for Testing
        seedAccountIfNotFound("admin@clinic.com", "12345678", "ADMIN");
        seedAccountIfNotFound("receptionist@clinic.com", "12345678", "RECEPTIONIST");
        seedAccountIfNotFound("doctor@clinic.com", "12345678", "DOCTOR");
        seedAccountIfNotFound("lab_tech@clinic.com", "12345678", "LAB_TECH");
        seedAccountIfNotFound("nurse@clinic.com", "12345678", "NURSE");
        seedAccountIfNotFound("patient1@clinic.com", "12345678", "PATIENT");

        // 4. Seed Default Settings if missing (since we might have truncated them or database is empty)
        seedDefaultSettings();
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

    private void seedDefaultSettings() {
        // Seed system_setting if missing
        seedSystemSettingIfNotFound("address", "71-73 Ngô Thời Nhiệm, Phường Võ Thị Sáu, Quận 3, TP.HCM", "Địa chỉ");
        seedSystemSettingIfNotFound("AI_MODERATION_ENABLED", "false", null);
        seedSystemSettingIfNotFound("clinicName", "Trustcare Clinic", "Tên phòng khám");
        seedSystemSettingIfNotFound("email", "cskh@clinic.com", "Email hỗ trợ");
        seedSystemSettingIfNotFound("operatingHours", "T2 – T7: 07:30 – 17:00, Chủ nhật: Nghỉ", "Giờ làm việc");
        seedSystemSettingIfNotFound("phone", "1900 2115", "Hotline 24/7");
        seedSystemSettingIfNotFound("website", "www.trustcare.vn", "Website");

        // Seed logo_setting if missing
        seedLogoSettingIfNotFound("main", "/images/logo.png");
        seedLogoSettingIfNotFound("login", "/images/logo.png");
    }

    private void seedSystemSettingIfNotFound(String key, String value, String description) {
        boolean exists = entityManager.createQuery(
                "SELECT COUNT(s) FROM SystemSetting s WHERE s.settingKey = :key", Long.class)
                .setParameter("key", key)
                .getSingleResult() > 0;
        if (!exists) {
            entityManager.createNativeQuery(
                "INSERT INTO system_setting (setting_key, setting_value, description, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())")
                .setParameter(1, key)
                .setParameter(2, value)
                .setParameter(3, description)
                .executeUpdate();
            log.info("Seeded missing system setting: {}", key);
        }
    }

    private void seedLogoSettingIfNotFound(String key, String url) {
        boolean exists = entityManager.createQuery(
                "SELECT COUNT(l) FROM LogoSetting l WHERE l.logoKey = :key", Long.class)
                .setParameter("key", key)
                .getSingleResult() > 0;
        if (!exists) {
            entityManager.createNativeQuery(
                "INSERT INTO logo_setting (logo_key, image_url, created_at, updated_at) VALUES (?, ?, NOW(), NOW())")
                .setParameter(1, key)
                .setParameter(2, url)
                .executeUpdate();
            log.info("Seeded missing logo setting: {}", key);
        }
    }
}