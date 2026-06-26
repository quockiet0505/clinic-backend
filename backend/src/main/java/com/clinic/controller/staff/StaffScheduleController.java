
package com.clinic.controller.staff;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.staff.StaffScheduleRequest;
import com.clinic.dto.staff.StaffScheduleResponse;
import com.clinic.service.staff.StaffScheduleService;
import com.clinic.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class StaffScheduleController {

    private final StaffScheduleService scheduleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ApiResponse<List<StaffScheduleResponse>> getAll() {
        return ResponseUtil.success(
                "Schedules fetched successfully",
                scheduleService.getAll()
        ).getBody();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<StaffScheduleResponse> create(
            @Valid @RequestBody StaffScheduleRequest request
    ) {
        return ResponseUtil.success(
                "Schedule created successfully",
                scheduleService.create(request)
        ).getBody();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<StaffScheduleResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody StaffScheduleRequest request
    ) {
        return ResponseUtil.success(
                "Schedule updated successfully",
                scheduleService.update(id, request)
        ).getBody();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {

        scheduleService.delete(id);

        return ResponseUtil.<Void>success(
                "Schedule deleted successfully",
                null
        ).getBody();
    }

    @PostMapping("/auto-generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> autoGenerate(@RequestParam int year, @RequestParam int month) {
        scheduleService.autoGenerateSchedules(year, month);
        return ResponseUtil.<Void>success("Schedules auto-generated successfully", null).getBody();
    }
}

