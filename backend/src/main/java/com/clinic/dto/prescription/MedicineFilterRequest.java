package com.clinic.dto.prescription;

import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MedicineFilterRequest extends BaseFilterRequest {
    private String sortBy = "name";
    private String sortDir = "ASC";
}
