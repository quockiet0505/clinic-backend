package com.clinic.controller.auth;

import com.clinic.dto.common.ApiResponse;
import com.clinic.entity.auth.Account;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.util.ResponseUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> updateAccountStatus(
            @PathVariable Integer id,
            @RequestParam Integer isActive
    ) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setIsActive(isActive);
        if (isActive == 1) {
            account.setFailedAttempt(0);
            account.setLockedUntil(null);
        }

        accountRepository.save(account);
        String message = (isActive == 1) ? "Account unlocked successfully" : "Account locked successfully";
        return ResponseUtil.success(message, message).getBody();
    }
}
