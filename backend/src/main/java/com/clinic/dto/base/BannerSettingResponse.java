package com.clinic.dto.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerSettingResponse {
    private Integer bannerId;
    private String bannerKey;
    private String imageUrl;
    private String linkUrl;
    private Integer displayOrder;
    private Boolean isActive;
}