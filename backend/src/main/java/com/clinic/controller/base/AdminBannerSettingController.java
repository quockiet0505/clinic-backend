package com.clinic.controller.base;

import com.clinic.dto.base.BannerSettingResponse;
import com.clinic.dto.common.ApiResponse;
import com.clinic.service.base.BannerSettingService;
import com.clinic.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/banners")
@RequiredArgsConstructor
public class AdminBannerSettingController {

    private final BannerSettingService bannerSettingService;

    @PostMapping("/{key}/upload")
    public ApiResponse<BannerSettingResponse> uploadBannerImage(
            @PathVariable String key,
            @RequestParam("file") MultipartFile file) {
        
        BannerSettingResponse response = bannerSettingService.updateBannerImage(key, file);
        return ResponseUtil.success("Banner updated successfully", response).getBody();
    }
}
