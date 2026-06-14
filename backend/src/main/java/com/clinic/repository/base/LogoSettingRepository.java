package com.clinic.repository.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.clinic.entity.base.LogoSetting;

@Repository
public interface LogoSettingRepository extends JpaRepository<LogoSetting, String> {
}
