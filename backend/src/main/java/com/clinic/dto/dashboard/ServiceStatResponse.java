package com.clinic.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceStatResponse {
    private Integer serviceId;
    private String serviceName;
    private Long totalOrders;
    private Long completedOrders;
    private Double completionRate; // %
    private Double revenue;
    private String imageUrl;
}