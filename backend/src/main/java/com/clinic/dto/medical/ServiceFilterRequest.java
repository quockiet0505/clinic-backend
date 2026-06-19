package com.clinic.dto.medical;

import com.clinic.common.enums.ServiceType;
import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceFilterRequest extends BaseFilterRequest {
    private ServiceType serviceType;

    public ServiceFilterRequest() {
        setSortBy("serviceName");
        setSortDir("ASC");
    }
}
