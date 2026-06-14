package com.clinic.controller.base;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.base.LogoSettingResponse;
import com.clinic.dto.common.ApiResponse;
import com.clinic.service.base.LogoSettingService;
import com.clinic.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/public/logos")
@RequiredArgsConstructor
public class LogoSettingController {

    private final LogoSettingService logoSettingService;

    @GetMapping
    public ApiResponse<List<LogoSettingResponse>> getAllLogos() {
        return ResponseUtil.success("Logos fetched successfully", logoSettingService.getAllLogos()).getBody();
    }

    @GetMapping("/{key}")
    public ApiResponse<LogoSettingResponse> getLogoByKey(@PathVariable String key) {
        LogoSettingResponse logo = logoSettingService.getLogoByKey(key);
        if (logo != null) {
            return ResponseUtil.success("Logo fetched successfully", logo).getBody();
        }
        return ResponseUtil.<LogoSettingResponse>error("Logo not found", null).getBody();
    }
}
