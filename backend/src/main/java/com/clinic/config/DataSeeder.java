package com.clinic.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.clinic.entity.auth.Account;
import com.clinic.entity.auth.Role;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.auth.RoleRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (accountRepository.existsByEmail("admin@clinic.com")) {
            return;
        }

        Role adminRole = roleRepository
                .findByRoleCode("ROLE_ADMIN")
                .orElseThrow(() ->
                        new RuntimeException("ROLE_ADMIN not found"));

        Account admin = new Account();

        admin.setEmail("admin@clinic.com");

        admin.setPassword(
                passwordEncoder.encode("Admin@123")
        );

        admin.setIsActive(1);

        admin.setRoles(Set.of(adminRole));

        accountRepository.save(admin);

        System.out.println("Admin account seeded!");
    }
}