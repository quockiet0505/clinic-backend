package com.clinic.dto.dashboard;

import com.clinic.dto.common.PageResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorStatsPageResponse {
    private long totalDoctors;
    private double totalRevenue;
    private double avgCompletionRate;
    private PageResponse<DoctorStatResponse> page;
}
