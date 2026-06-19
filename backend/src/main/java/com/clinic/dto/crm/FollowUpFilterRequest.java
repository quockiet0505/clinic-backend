package com.clinic.dto.crm;

import com.clinic.common.enums.FollowUpStatus;
import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FollowUpFilterRequest extends BaseFilterRequest {
    private FollowUpStatus status;
    private String tab;

    public FollowUpFilterRequest() {
        setSortBy("scheduledDatetime");
        setSortDir("DESC");
    }
}
