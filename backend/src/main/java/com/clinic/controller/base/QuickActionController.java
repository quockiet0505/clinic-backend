package com.clinic.controller.base;

import com.clinic.dto.base.QuickActionResponse;
import com.clinic.dto.common.ApiResponse;
import com.clinic.service.base.QuickActionService;
import com.clinic.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/quick-actions")
@RequiredArgsConstructor
public class QuickActionController {

    private final QuickActionService quickActionService;

    @GetMapping
    public ApiResponse<List<QuickActionResponse>> getAllQuickActions() {
        return ResponseUtil.success("Quick actions fetched successfully",
                quickActionService.getAllActiveQuickActions()).getBody();
    }

    @GetMapping("/{id}")
    public ApiResponse<QuickActionResponse> getQuickActionById(@PathVariable Integer id) {
        QuickActionResponse action = quickActionService.getQuickActionById(id);
        if (action != null) {
            return ResponseUtil.success("Quick action fetched", action).getBody();
        }
        return ResponseUtil.<QuickActionResponse>error("Quick action not found", null).getBody();
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<QuickActionResponse> getQuickActionBySlug(@PathVariable String slug) {
        QuickActionResponse action = quickActionService.getQuickActionBySlug(slug);
        if (action != null) {
            return ResponseUtil.success("Quick action fetched", action).getBody();
        }
        return ResponseUtil.<QuickActionResponse>error("Quick action not found", null).getBody();
    }
}