package com.clinic.controller.common;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.common.ApiResponse;
import com.clinic.util.ResponseUtil;

@RestController
@RequestMapping("/api/v1/static")
public class StaticDataController {

    @GetMapping("/quick-actions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getQuickActions() {

        List<Map<String, Object>> actions = List.of(
                Map.of(
                        "id", 1,
                        "title", "Đặt khám tại cơ sở",
                        "iconUrl", "/icons/quick-actions/dat-kham-tai-co-so.png"
                ),
                Map.of(
                        "id", 2,
                        "title", "Đặt khám chuyên khoa",
                        "iconUrl", "/icons/quick-actions/dat-kham-chuyen-khoa.png"
                ),
                Map.of(
                        "id", 3,
                        "title", "Đặt lịch xét nghiệm",
                        "iconUrl", "/icons/quick-actions/dat-lich-xet-nghiem.png"
                ),
                Map.of(
                        "id", 4,
                        "title", "Đặt khám ngoài giờ",
                        "iconUrl", "/icons/quick-actions/dat-kham-ngoai-gio.png"
                ),
                Map.of(
                        "id", 5,
                        "title", "Đặt khám theo bác sĩ",
                        "iconUrl", "/icons/quick-actions/dat-kham-theo-bac-si.png"
                )
        );

        return ResponseUtil.success(
                "Quick actions retrieved successfully",
                actions
        );
    }

    @GetMapping("/logo")
    public ResponseEntity<ApiResponse<Map<String, String>>> getLogo() {

        return ResponseUtil.success(
                "Logo retrieved successfully",
                Map.of(
                        "logoUrl",
                        "/images/logos/logo.png"
                )
        );
    }

    @GetMapping("/banner")
    public ResponseEntity<ApiResponse<Map<String, String>>> getBanner() {

        return ResponseUtil.success(
                "Banner retrieved successfully",
                Map.of(
                        "bannerUrl",
                        "/images/banners/hero-banner.webp"
                )
        );
    }
}