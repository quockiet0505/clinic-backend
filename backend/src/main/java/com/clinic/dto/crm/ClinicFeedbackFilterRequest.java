package com.clinic.dto.crm;

import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClinicFeedbackFilterRequest extends BaseFilterRequest {
    private Integer rating;

    public ClinicFeedbackFilterRequest() {
        setSortBy("createdAt");
        setSortDir("DESC");
    }
}
