package com.clinic.service.base;

import com.clinic.entity.base.SystemSetting;
import com.clinic.repository.base.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemSettingService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    public Map<String, String> getAllSettingsAsMap() {
        List<SystemSetting> settings = systemSettingRepository.findAll();
        Map<String, String> map = new HashMap<>();
        for (SystemSetting setting : settings) {
            map.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return map;
    }

    public void updateSettings(Map<String, String> settingsMap) {
        for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
            SystemSetting setting = systemSettingRepository.findById(entry.getKey()).orElse(new SystemSetting());
            setting.setSettingKey(entry.getKey());
            setting.setSettingValue(entry.getValue());
            systemSettingRepository.save(setting);
        }
    }
}
