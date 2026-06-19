package com.clinic.dto.staff;

import com.clinic.common.enums.StaffType;
import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StaffFilterRequest extends BaseFilterRequest {
    private StaffType staffType;
    private Integer expertiseId;
    private Integer isActive;
    private Integer minRating;

    public StaffFilterRequest() {
        setSortBy("fullName");
        setSortDir("ASC");
    }
}
