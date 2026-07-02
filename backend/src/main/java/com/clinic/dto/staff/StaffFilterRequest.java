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
    private String expertiseName;
    private Integer isActive;
    private Integer minRating;
    private String gender;
    private java.math.BigDecimal minPrice;
    private java.math.BigDecimal maxPrice;

    public StaffFilterRequest() {
        setSortBy("fullName");
        setSortDir("ASC");
    }
}
