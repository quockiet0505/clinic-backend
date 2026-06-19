package com.clinic.dto.dashboard;

import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DashboardPeriodFilterRequest extends BaseFilterRequest {
    private Integer month;
    private Integer year;
}
