package com.clinic.dto.auth;

import com.clinic.dto.common.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleFilterRequest extends BaseFilterRequest {

    public RoleFilterRequest() {
        setSortBy("roleName");
        setSortDir("ASC");
    }
}
