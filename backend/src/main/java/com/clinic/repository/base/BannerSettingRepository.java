package com.clinic.repository.base;

import com.clinic.entity.base.BannerSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BannerSettingRepository extends JpaRepository<BannerSetting, Integer> {
    Optional<BannerSetting> findByBannerKey(String bannerKey);
}