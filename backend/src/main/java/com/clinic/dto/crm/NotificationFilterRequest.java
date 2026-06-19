package com.clinic.dto.crm;

import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationFilterRequest extends BaseFilterRequest {
    private String type;

    public NotificationFilterRequest() {
        setSortBy("sentAt");
        setSortDir("DESC");
    }
}
