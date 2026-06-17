package com.clinic.dto.staff;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ExpertiseResponse {

    private Integer expertiseId;

    private String expertiseName;

    private String iconUrl;

    // ExpertiseResponse.java
    private Integer doctorCount;

    private LocalDateTime createdAt;
}