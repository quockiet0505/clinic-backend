package com.clinic.dto.medical;

import com.clinic.common.enums.InvoiceStatus;
import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InvoiceFilterRequest extends BaseFilterRequest {
    private InvoiceStatus status;
    private Integer patientId;
    private String patientName;
}
