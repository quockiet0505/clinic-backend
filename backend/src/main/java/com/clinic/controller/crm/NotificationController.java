package com.clinic.controller.crm;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.crm.NotificationResponse;
import com.clinic.security.CustomUserDetails;
import com.clinic.service.crm.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Get notifications for the currently logged-in user
    @GetMapping("/my-notifications")
    @PreAuthorize("isAuthenticated()") // Any logged-in user can view their notifications
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer accountId = userDetails.getAccount().getAccountId();
        return ResponseEntity.ok(notificationService.getMyNotifications(accountId));
    }
}