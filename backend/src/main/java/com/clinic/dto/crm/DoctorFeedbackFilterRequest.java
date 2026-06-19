package com.clinic.dto.crm;

import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DoctorFeedbackFilterRequest extends BaseFilterRequest {
    private Integer rating;

    public DoctorFeedbackFilterRequest() {
        setSortBy("createdAt");
        setSortDir("DESC");
    }
}
