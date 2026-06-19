package com.clinic.dto.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickActionResponse {
    private Integer actionId;
    private String title;
    private String slug;
    private String iconUrl;
    private Integer displayOrder;
    private Boolean isActive;
}