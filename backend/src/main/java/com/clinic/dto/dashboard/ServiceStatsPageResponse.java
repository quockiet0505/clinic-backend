package com.clinic.dto.dashboard;

import com.clinic.dto.common.PageResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceStatsPageResponse {
    private long totalServices;
    private long totalOrders;
    private double totalRevenue;
    private PageResponse<ServiceStatResponse> page;
}
