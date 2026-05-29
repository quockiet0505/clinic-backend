package com.clinic.controller.crm;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.crm.NotificationResponse;
import com.clinic.security.CustomUserDetails;
import com.clinic.service.crm.NotificationService;
import com.clinic.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my-notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>>
    getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Integer accountId =
                userDetails.getAccount().getAccountId();

        List<NotificationResponse> notifications =
                notificationService.getMyNotifications(accountId);

        return ResponseUtil.success(
                "Notifications retrieved successfully",
                notifications
        );
    }
}