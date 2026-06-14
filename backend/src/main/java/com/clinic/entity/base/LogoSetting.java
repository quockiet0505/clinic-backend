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
@Table(name = "logo_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogoSetting extends BaseEntity {

    @Id
    @Column(name = "logo_key", nullable = false, length = 50)
    private String logoKey;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;
}
