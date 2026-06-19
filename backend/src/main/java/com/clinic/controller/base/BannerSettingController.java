package com.clinic.controller.base;

import com.clinic.dto.base.BannerSettingResponse;
import com.clinic.dto.common.ApiResponse;
import com.clinic.service.base.BannerSettingService;
import com.clinic.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/banners")
@RequiredArgsConstructor
public class BannerSettingController {

    private final BannerSettingService bannerSettingService;

    @GetMapping
    public ApiResponse<List<BannerSettingResponse>> getAllBanners() {
        return ResponseUtil.success("Banners fetched successfully", bannerSettingService.getAllBanners()).getBody();
    }

    @GetMapping("/{key}")
    public ApiResponse<BannerSettingResponse> getBannerByKey(@PathVariable String key) {
        BannerSettingResponse banner = bannerSettingService.getBannerByKey(key);
        if (banner != null) {
            return ResponseUtil.success("Banner fetched successfully", banner).getBody();
        }
        return ResponseUtil.<BannerSettingResponse>error("Banner not found", null).getBody();
    }
}