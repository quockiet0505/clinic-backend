package com.clinic.controller.common;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/static")
public class StaticDataController {

    @GetMapping("/quick-actions")
    public ResponseEntity<List<Map<String, Object>>> getQuickActions() {
        List<Map<String, Object>> actions = List.of(
            Map.of("id", 1, "title", "Đặt khám tại cơ sở", "iconUrl", "/icons/quick-actions/dat-kham-tai-co-so.png"),
            Map.of("id", 2, "title", "Đặt khám chuyên khoa", "iconUrl", "/icons/quick-actions/dat-kham-chuyen-khoa.png"),
            Map.of("id", 3, "title", "Đặt lịch xét nghiệm", "iconUrl", "/icons/quick-actions/dat-lich-xet-nghiem.png"),
            Map.of("id", 4, "title", "Đặt khám ngoài giờ", "iconUrl", "/icons/quick-actions/dat-kham-ngoai-gio.png"),
            Map.of("id", 5, "title", "Đặt khám theo bác sĩ", "iconUrl", "/icons/quick-actions/dat-kham-theo-bac-si.png")
        );
        return ResponseEntity.ok(actions);
    }

    @GetMapping("/logo")
    public ResponseEntity<Map<String, String>> getLogo() {
        return ResponseEntity.ok(Map.of("logoUrl", "/images/logos/logo.png"));
    }

    @GetMapping("/banner")
    public ResponseEntity<Map<String, String>> getBanner() {
        return ResponseEntity.ok(Map.of("bannerUrl", "/images/banners/hero-banner.webp"));
    }
}