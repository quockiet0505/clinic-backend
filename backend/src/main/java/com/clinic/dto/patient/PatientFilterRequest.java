package com.clinic.dto.patient;

import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PatientFilterRequest extends BaseFilterRequest {
    private String gender;

    public PatientFilterRequest() {
        setSortBy("fullName");
        setSortDir("ASC");
    }
}
