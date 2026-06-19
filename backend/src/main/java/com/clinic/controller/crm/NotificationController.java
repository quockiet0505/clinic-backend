package com.clinic.controller.crm;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.crm.NotificationFilterRequest;
import com.clinic.dto.crm.NotificationResponse;
import com.clinic.dto.crm.NotificationRequest;
import com.clinic.service.crm.NotificationService;
import com.clinic.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getAll(
            @ModelAttribute NotificationFilterRequest filter
    ) {
        return ResponseUtil.success("Notifications retrieved successfully", notificationService.getAll(filter));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllLegacy() {
        return ResponseUtil.success("Notifications retrieved successfully", notificationService.getAllLegacy());
    }

    @PostMapping
    public ApiResponse<Void> createNotification(@RequestBody NotificationRequest request) {
        notificationService.createNotification(request);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Notification created")
                .build();
    }
}
