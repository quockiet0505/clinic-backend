package com.clinic.dto.staff;



import com.clinic.common.enums.LeaveStatus;

import com.clinic.common.enums.LeaveType;

import com.clinic.common.enums.StaffType;

import com.clinic.dto.common.BaseFilterRequest;

import lombok.Data;

import lombok.EqualsAndHashCode;



@Data

@EqualsAndHashCode(callSuper = true)

public class LeaveRequestFilterRequest extends BaseFilterRequest {

    private LeaveStatus status;

    private LeaveType leaveType;

    private StaffType staffType;

    /** today | pending | processed */
    private String tab;

    private Integer staffId;

    public LeaveRequestFilterRequest() {

        setSortBy("fromDate");

        setSortDir("DESC");

    }

}

