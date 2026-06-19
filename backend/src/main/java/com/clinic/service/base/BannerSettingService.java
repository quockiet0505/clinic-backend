package com.clinic.service.base;

import com.clinic.dto.base.BannerSettingResponse;
import com.clinic.entity.base.BannerSetting;
import com.clinic.repository.base.BannerSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerSettingService {

    private final BannerSettingRepository bannerSettingRepository;

    @Transactional(readOnly = true)
    public List<BannerSettingResponse> getAllBanners() {
        return bannerSettingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BannerSettingResponse getBannerByKey(String key) {
        return bannerSettingRepository.findByBannerKey(key)
                .map(this::toResponse)
                .orElse(null);
    }

    private BannerSettingResponse toResponse(BannerSetting entity) {
        return BannerSettingResponse.builder()
                .bannerId(entity.getBannerId())
                .bannerKey(entity.getBannerKey())
                .imageUrl(entity.getImageUrl())
                .linkUrl(entity.getLinkUrl())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.getIsActive())
                .build();
    }
}