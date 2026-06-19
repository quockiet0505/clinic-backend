package com.clinic.dto.medical;

import com.clinic.common.enums.MedicalRecordStatus;
import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MedicalRecordFilterRequest extends BaseFilterRequest {
    private MedicalRecordStatus status;
    private Integer doctorId;
    /** active = PENDING/IN_PROGRESS/WAITING_RESULT; archived = DONE/CANCELLED */
    private String tab;
}
