package com.clinic.service.base;

import com.clinic.dto.base.BannerSettingResponse;
import com.clinic.entity.base.BannerSetting;
import com.clinic.repository.base.BannerSettingRepository;
import com.clinic.service.common.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerSettingService {

    private final BannerSettingRepository bannerSettingRepository;
    private final FileStorageService fileStorageService;

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

    @Transactional
    public BannerSettingResponse updateBannerImage(String key, MultipartFile file) {
        String fileUrl = fileStorageService.storeFile(file);
        
        BannerSetting banner = bannerSettingRepository.findByBannerKey(key).orElse(null);
        if (banner == null) {
            banner = BannerSetting.builder()
                    .bannerKey(key)
                    .imageUrl(fileUrl)
                    .isActive(true)
                    .displayOrder(1)
                    .build();
        } else {
            banner.setImageUrl(fileUrl);
        }
        
        bannerSettingRepository.save(banner);
        return toResponse(banner);
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