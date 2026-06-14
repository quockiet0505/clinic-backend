package com.clinic.dto.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoSettingResponse {
    private String logoKey;
    private String imageUrl;
}
