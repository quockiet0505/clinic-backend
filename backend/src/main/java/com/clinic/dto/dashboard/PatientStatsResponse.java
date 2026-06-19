package com.clinic.dto.dashboard;

import com.clinic.dto.common.PageResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientStatsResponse {
    private long newPatients;
    private long returningPatients;
    private PageResponse<TopPatient> topPatients;

    @Data
    @Builder
    public static class TopPatient {
        private Integer patientId;
        private String patientName;
        private long visitCount;
        private double totalSpent;
        private String lastVisit;
        private String avatarUrl;
    }
}