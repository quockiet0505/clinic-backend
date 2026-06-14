package com.clinic.service.base;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.dto.base.LogoSettingResponse;
import com.clinic.repository.base.LogoSettingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogoSettingService {

    private final LogoSettingRepository logoSettingRepository;

    @Transactional(readOnly = true)
    public List<LogoSettingResponse> getAllLogos() {
        return logoSettingRepository.findAll().stream()
                .map(setting -> new LogoSettingResponse(setting.getLogoKey(), setting.getImageUrl()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LogoSettingResponse getLogoByKey(String key) {
        return logoSettingRepository.findById(key)
                .map(setting -> new LogoSettingResponse(setting.getLogoKey(), setting.getImageUrl()))
                .orElse(null);
    }
}
