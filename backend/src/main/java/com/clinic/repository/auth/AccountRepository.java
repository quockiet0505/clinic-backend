package com.clinic.repository.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.auth.Account;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);
    
    // Used to check for duplicate emails during registration
    boolean existsByEmail(String email);
}