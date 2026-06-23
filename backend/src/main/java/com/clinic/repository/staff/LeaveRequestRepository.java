package com.clinic.repository.staff;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.clinic.common.enums.LeaveStatus;
import com.clinic.entity.staff.LeaveRequest;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer>, JpaSpecificationExecutor<LeaveRequest> {
    // Find all leave requests by a specific staff member
    List<LeaveRequest> findByStaff_StaffId(Integer staffId);
    
    // Find leave requests by status (e.g., to view all PENDING requests)
    List<LeaveRequest> findByStatus(LeaveStatus status);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LeaveRequest l " +
           "WHERE l.staff.staffId = :staffId " +
           "AND l.status = 'APPROVED' " +
           "AND l.fromDate <= :date " +
           "AND l.toDate >= :date")
    boolean isDoctorOnLeave(@Param("staffId") Integer staffId, @Param("date") LocalDate date);
}