package com.clinic.service.staff;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.LeaveStatus;
import com.clinic.dto.staff.LeaveRequestRequest;
import com.clinic.dto.staff.LeaveRequestResponse;
import com.clinic.entity.staff.LeaveRequest;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.staff.LeaveRequestMapper;
import com.clinic.repository.staff.LeaveRequestRepository;
import com.clinic.repository.staff.StaffRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StaffRepository staffRepository;
    private final LeaveRequestMapper leaveRequestMapper;

    /**
     * Creates a new leave request for a staff member.
     * The default status is set to PENDING.
     */
    @Transactional
    public LeaveRequestResponse create(LeaveRequestRequest request) {
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff member not found."));

        LeaveRequest leaveRequest = leaveRequestMapper.toEntity(request);
        leaveRequest.setStaff(staff);
        leaveRequest.setStatus(LeaveStatus.PENDING); // Always pending on creation

        return leaveRequestMapper.toResponse(leaveRequestRepository.save(leaveRequest));
    }

    /**
     * Retrieves all leave requests in the system.
     */
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getAll() {
        return leaveRequestRepository.findAll().stream()
                .map(leaveRequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Admin or Manager reviews the leave request (APPROVE or REJECT).
     */
    @Transactional
    public LeaveRequestResponse reviewLeaveRequest(Integer leaveId, LeaveStatus newStatus, Integer approverId, String rejectionReason) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found."));
        
        Staff approver = staffRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found."));

        leaveRequest.setStatus(newStatus);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setReviewedAt(LocalDateTime.now());
        
        // Log the reason if the request is rejected
        if (newStatus == LeaveStatus.REJECTED) {
            leaveRequest.setRejectionReason(rejectionReason);
        }

        return leaveRequestMapper.toResponse(leaveRequestRepository.save(leaveRequest));
    }
}