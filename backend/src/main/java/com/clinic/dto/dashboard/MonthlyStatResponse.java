// src/main/java/com/clinic/dto/dashboard/MonthlyStatResponse.java
package com.clinic.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlyStatResponse {
    private String name;
    private long completed;
    private long cancelled;
    private long rescheduled;
}