package com.clinic.repository.staff;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.clinic.common.enums.LeaveStatus;
import com.clinic.entity.staff.LeaveRequest;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer>, JpaSpecificationExecutor<LeaveRequest> {
    // Find all leave requests by a specific staff member
    List<LeaveRequest> findByStaff_StaffId(Integer staffId);
    
    // Find leave requests by status (e.g., to view all PENDING requests)
    List<LeaveRequest> findByStatus(LeaveStatus status);
}