package com.clinic.dto.medical;

import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceOrderFilterRequest extends BaseFilterRequest {
    private ServiceOrderStatus status;
}
