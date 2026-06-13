package com.clinic.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "system_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemSetting extends BaseEntity {

    @Id
    @Column(name = "setting_key", nullable = false, length = 50)
    private String settingKey;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;
    
    @Column(name = "description", length = 255)
    private String description;
}
