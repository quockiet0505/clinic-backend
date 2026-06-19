package com.clinic.dto.prescription;

import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PrescriptionFilterRequest extends BaseFilterRequest {
    private String status;
    private String sortBy = "createdAt";
    private String sortDir = "DESC";
}
