package com.clinic.dto.dashboard;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RevenueStatsResponse {
    private double totalRevenue;
    private double consultationRevenue;
    private double serviceRevenue;
    private List<MonthlyTrend> monthlyTrend;
    private List<ServiceRevenue> byService;

    @Data
    @Builder
    public static class MonthlyTrend {
        private String name;
        private double revenue;
    }

    @Data
    @Builder
    public static class ServiceRevenue {
        private String serviceName;
        private double revenue;
        private double percentage;
    }
}