package com.clinic.service.staff;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.LeaveStatus;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.staff.LeaveRequestFilterRequest;
import com.clinic.dto.staff.LeaveRequestRequest;
import com.clinic.dto.staff.LeaveRequestResponse;
import com.clinic.entity.staff.LeaveRequest;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.staff.LeaveRequestMapper;
import com.clinic.repository.staff.LeaveRequestRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.specification.staff.LeaveRequestSpecification;
import com.clinic.util.FilterUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StaffRepository staffRepository;
    private final LeaveRequestMapper leaveRequestMapper;

    @Transactional
    public LeaveRequestResponse create(LeaveRequestRequest request, String userEmail) {
        Staff staff;
        if (request.getStaffId() != null) {
            staff = staffRepository.findById(request.getStaffId())
                    .orElseThrow(() -> new RuntimeException("Staff member not found."));
        } else {
            staff = staffRepository.findByAccount_Email(userEmail)
                    .orElseThrow(() -> new RuntimeException("Logged in staff member not found."));
        }

        LeaveRequest leaveRequest = leaveRequestMapper.toEntity(request);
        leaveRequest.setStaff(staff);
        leaveRequest.setStatus(LeaveStatus.PENDING);

        return leaveRequestMapper.toResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveRequestResponse> getAll(LeaveRequestFilterRequest filter) {
        Specification<LeaveRequest> spec = LeaveRequestSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<LeaveRequest> page = leaveRequestRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(leaveRequestMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getAllLegacy() {
        return leaveRequestRepository.findAll().stream()
                .map(leaveRequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequestResponse reviewLeaveRequest(Integer leaveId, LeaveStatus newStatus, Integer approverId, String rejectionReason) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found."));
        
        Staff approver = staffRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found."));

        leaveRequest.setStatus(newStatus);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setReviewedAt(LocalDateTime.now());
        
        if (newStatus == LeaveStatus.REJECTED) {
            leaveRequest.setRejectionReason(rejectionReason);
        }

        return leaveRequestMapper.toResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional
    public LeaveRequestResponse reviewLeaveRequest(Integer leaveId, LeaveStatus newStatus, String approverEmail, String rejectionReason) {
        Staff approver = staffRepository.findByAccount_Email(approverEmail)
                .orElseThrow(() -> new RuntimeException("Approver staff member not found."));
        return reviewLeaveRequest(leaveId, newStatus, approver.getStaffId(), rejectionReason);
    }

    @Transactional
    public void delete(Integer id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Không thể hủy đơn đã được xử lý");
        }

        LocalDate fromDate = leaveRequest.getFromDate();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime deadline = fromDate.atStartOfDay().minusDays(1).withHour(12).withMinute(0).withSecond(0);

        if (now.isAfter(deadline)) {
            throw new RuntimeException("Chỉ có thể hủy đơn trước 12h ngày hôm trước ngày bắt đầu nghỉ");
        }

        leaveRequestRepository.deleteById(id);
    }
}
