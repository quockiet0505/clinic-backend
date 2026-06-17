package com.clinic.service.staff;

import java.time.LocalDate;
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

    @Transactional
    public LeaveRequestResponse create(LeaveRequestRequest request) {
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff member not found."));

        LeaveRequest leaveRequest = leaveRequestMapper.toEntity(request);
        leaveRequest.setStaff(staff);
        leaveRequest.setStatus(LeaveStatus.PENDING);

        return leaveRequestMapper.toResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getAll() {
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
    public void delete(Integer id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        // Chỉ cho phép hủy khi trạng thái PENDING
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Không thể hủy đơn đã được xử lý");
        }

        // Kiểm tra điều kiện thời gian: chỉ được hủy trước 12h ngày hôm trước (so với fromDate)
        LocalDate fromDate = leaveRequest.getFromDate();
        LocalDateTime now = LocalDateTime.now();

        // Hạn chót: 12:00 của ngày trước ngày bắt đầu nghỉ
        LocalDateTime deadline = fromDate.atStartOfDay().minusDays(1).withHour(12).withMinute(0).withSecond(0);

        if (now.isAfter(deadline)) {
            throw new RuntimeException("Chỉ có thể hủy đơn trước 12h ngày hôm trước ngày bắt đầu nghỉ");
        }

        // Nếu tất cả điều kiện thỏa mãn, thực hiện xóa
        leaveRequestRepository.deleteById(id);
    }
}