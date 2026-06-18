package com.clinic.dto.common;

import lombok.Data;

@Data
public class BaseFilterRequest {
    private String search;
    private String fromDate;
    private String toDate;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDir = "DESC";
}