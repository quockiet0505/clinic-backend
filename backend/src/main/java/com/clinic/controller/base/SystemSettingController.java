package com.clinic.controller.base;

import com.clinic.dto.common.ApiResponse;
import com.clinic.service.base.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
public class SystemSettingController {

    @Autowired
    private SystemSettingService systemSettingService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getAllSettings() {
        Map<String, String> settings = systemSettingService.getAllSettingsAsMap();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> updateSettings(@RequestBody Map<String, String> settings) {
        systemSettingService.updateSettings(settings);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
