package com.clinic.controller.crm;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.crm.NotificationResponse;
import com.clinic.dto.crm.NotificationRequest;
import com.clinic.service.crm.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getNotifications(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        List<NotificationResponse> data = notificationService.getNotifications(search, type, fromDate, toDate);
        return ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Success")
                .data(data)
                .build();
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