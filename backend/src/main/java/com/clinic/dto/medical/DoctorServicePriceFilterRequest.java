package com.clinic.dto.medical;

import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DoctorServicePriceFilterRequest extends BaseFilterRequest {

    public DoctorServicePriceFilterRequest() {
        setSortBy("staff.fullName");
        setSortDir("ASC");
    }
}
