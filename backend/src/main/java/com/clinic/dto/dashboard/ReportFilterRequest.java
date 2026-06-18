package com.clinic.dto.dashboard;

import lombok.Data;

@Data
public class ReportFilterRequest {
    private String type; // 'all', 'overview', 'doctors', 'services', 'patients', 'revenue'
    private String period; // 'month', 'quarter'
    private Integer month;
    private Integer quarter;
    private Integer year;
    private String format; // 'pdf', 'excel'
}