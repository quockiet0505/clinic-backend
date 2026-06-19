package com.clinic.dto.staff;

import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExpertiseFilterRequest extends BaseFilterRequest {

    public ExpertiseFilterRequest() {
        setSortBy("expertiseName");
        setSortDir("ASC");
    }
}
