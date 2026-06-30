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

import com.clinic.entity.base.LogoSetting;
import com.clinic.entity.base.BannerSetting;
import com.clinic.entity.base.QuickAction;
import com.clinic.repository.base.LogoSettingRepository;
import com.clinic.repository.base.BannerSettingRepository;
import com.clinic.repository.base.QuickActionRepository;

import org.springframework.core.annotation.Order;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class DatabaseSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogoSettingRepository logoSettingRepository;
    private final BannerSettingRepository bannerSettingRepository;
    private final QuickActionRepository quickActionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Initialize System Roles (KHÔNG có tiền tố ROLE_)
        seedRoleIfNotFound("ADMIN", "Administrator");
        seedRoleIfNotFound("DOCTOR", "Doctor");
        seedRoleIfNotFound("STAFF", "Clinic Staff");
        seedRoleIfNotFound("PATIENT", "Patient");

        // 2. Initialize Master Admin Account
        String adminEmail = "kiet@gmail.com";
        if (!accountRepository.existsByEmail(adminEmail)) {
            log.info("No admin account found. Seeding default Master Admin...");

            Account admin = new Account();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("12345678"));
            admin.setIsActive(1);
            admin.setFailedAttempt(0);

            // Lấy role ADMIN (mã "ADMIN")
            Role adminRole = roleRepository.findByRoleCode("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found during seeding"));
            
            admin.getRoles().add(adminRole);

            accountRepository.save(admin);
            log.info(" Master Admin account created successfully! Email: {} | Password: 12345678", adminEmail);
        } else {
            log.info("Database already contains the admin account. Skipping seeder.");
        }

        // 3. Seed Default Logos, Banners, and Quick Actions
        seedDefaultLogos();
        seedDefaultBanners();
        seedDefaultQuickActions();
    }

    private void seedDefaultLogos() {
        if (logoSettingRepository.count() == 0) {
            log.info("Seeding default logo settings...");
            logoSettingRepository.save(new LogoSetting("main", "/images/logo.png"));
            logoSettingRepository.save(new LogoSetting("login", "/images/logo.png"));
            logoSettingRepository.save(new LogoSetting("favicon", "/images/logo.png"));
        }
    }

    private void seedDefaultBanners() {
        if (bannerSettingRepository.count() == 0) {
            log.info("Seeding default banner settings...");
            bannerSettingRepository.save(BannerSetting.builder().bannerKey("main").imageUrl("/images/banners/hero-banner.jpg").isActive(true).displayOrder(1).build());
            bannerSettingRepository.save(BannerSetting.builder().bannerKey("doctor").imageUrl("/images/banners/doctor.webp").isActive(true).displayOrder(2).build());
            bannerSettingRepository.save(BannerSetting.builder().bannerKey("service").imageUrl("/images/banners/service.jpg").isActive(true).displayOrder(3).build());
        }
    }

    private void seedDefaultQuickActions() {
        if (quickActionRepository.count() == 0) {
            log.info("Seeding default quick actions...");
            quickActionRepository.save(QuickAction.builder().title("Đặt khám Bác sĩ").slug("dat-kham-bac-si").iconUrl("/icons/quick-actions/dat-kham-theo-bac-si.png").isActive(true).displayOrder(1).build());
            quickActionRepository.save(QuickAction.builder().title("Đặt khám Chuyên khoa").slug("dat-kham-chuyen-khoa").iconUrl("/icons/quick-actions/dat-kham-chuyen-khoa.png").isActive(true).displayOrder(2).build());
            quickActionRepository.save(QuickAction.builder().title("Đặt lịch Xét nghiệm").slug("dat-lich-xet-nghiem").iconUrl("/icons/quick-actions/dat-lich-xet-nghiem.png").isActive(true).displayOrder(3).build());
            quickActionRepository.save(QuickAction.builder().title("Tư vấn sức khoẻ").slug("tu-van-suc-khoe").iconUrl("/icons/quick-actions/dat-kham-tai-co-so.png").isActive(true).displayOrder(4).build());
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