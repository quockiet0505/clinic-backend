package com.clinic.dto.dashboard;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PatientStatsResponse {
    private long newPatients;
    private long returningPatients;
    private List<TopPatient> topPatients;

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