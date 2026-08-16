// src/main/java/com/clinic/service/appointment/AppointmentService.java
package com.clinic.service.appointment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.AppointmentType;
import com.clinic.common.enums.BookingMode;
import com.clinic.common.enums.CancelledByType;
import com.clinic.common.enums.ScheduleStatus;
import com.clinic.common.enums.ServiceType;
import com.clinic.common.enums.StaffType;
import com.clinic.dto.appointment.AppointmentFilterRequest;
import com.clinic.dto.appointment.AppointmentRequest;
import com.clinic.dto.appointment.AppointmentResponse;
import com.clinic.dto.appointment.TimeSlotResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Expertise;
import com.clinic.entity.staff.Staff;
import com.clinic.entity.staff.StaffSchedule;
import com.clinic.mapper.appointment.AppointmentMapper;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.medical.ServiceRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.staff.ExpertiseRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.repository.staff.StaffScheduleRepository;
import com.clinic.repository.staff.LeaveRequestRepository;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.medical.ServiceOrderRepository;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.medical.ServiceOrder;
import com.clinic.common.enums.MedicalRecordStatus;
import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.specification.appointment.AppointmentSpecification;
import com.clinic.util.FilterUtils;
import com.clinic.util.HolidayUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final ServiceRepository serviceRepository;
    private final ExpertiseRepository expertiseRepository;
    private final StaffScheduleRepository scheduleRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final AppointmentMapper appointmentMapper;
    private final com.clinic.service.crm.NotificationService notificationService;

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getAll(AppointmentFilterRequest filter) {
        Specification<Appointment> spec = AppointmentSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<Appointment> page = appointmentRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(this::enrichResponse));
    }

    private AppointmentResponse enrichResponse(Appointment appointment) {
        AppointmentResponse response = appointmentMapper.toResponse(appointment);
        if (appointment.getMainDoctor() != null && appointment.getAppointmentDate() != null) {
            boolean isBusy = leaveRequestRepository.isDoctorOnLeave(appointment.getMainDoctor().getStaffId(), appointment.getAppointmentDate());
            response.setIsDoctorBusy(isBusy);
        }
        return response;
    }

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Patient patient = patientRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        if (patient.getAccount().getIsActive() == 0) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa.");
        }
        return patient;
    }

    private BookingMode resolveBookingMode(AppointmentRequest request) {
        if (request.getBookingMode() != null) {
            if (request.getBookingMode() == BookingMode.EXPERTISE) {
                throw new RuntimeException(
                        "Chỉ hỗ trợ 2 luồng đặt lịch: DOCTOR (chọn chuyên khoa + bác sĩ) hoặc SERVICE (xét nghiệm/chụp).");
            }
            return request.getBookingMode();
        }
        if (request.getServiceId() != null) {
            return BookingMode.SERVICE;
        }
        if (request.getMainDoctorId() != null || request.getExpertiseId() != null) {
            return BookingMode.DOCTOR;
        }
        throw new RuntimeException("Cannot determine booking mode. Must provide Doctor+Expertise or Service.");
    }

    private Expertise loadExpertise(Integer expertiseId) {
        if (expertiseId == null) {
            return null;
        }
        return expertiseRepository.findById(expertiseId)
                .orElseThrow(() -> new RuntimeException("Expertise not found"));
    }

    private Staff loadDoctor(Integer doctorId) {
        return staffRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    private Staff autoAssignTechnician(LocalDate date, LocalTime slotStart, Integer durationMinutes) {
        List<Staff> techs = staffRepository.findByStaffTypeAndIsDeleted(StaffType.LAB_TECH, 0);
        if (techs.isEmpty()) {
            throw new RuntimeException("No lab technician available.");
        }
        
        LocalTime slotEnd = slotStart.plusMinutes(durationMinutes != null ? durationMinutes : 30);

        for (Staff tech : techs) {
            if (!leaveRequestRepository.isDoctorOnLeave(tech.getStaffId(), date)) {
                if (!isSlotTakenByDoctor(tech.getStaffId(), date, slotStart, slotEnd)) {
                    return tech;
                }
            }
        }
        throw new RuntimeException("All lab technicians are busy at the selected time.");
    }

    private boolean isSlotTakenByDoctor(Integer doctorId, LocalDate date, LocalTime slotStart, LocalTime slotEnd) {
        return isSlotTakenByDoctor(doctorId, date, slotStart, slotEnd, null);
    }

    private boolean isSlotTakenByDoctor(Integer doctorId, LocalDate date, LocalTime slotStart, LocalTime slotEnd, Integer excludeAppointmentId) {
        List<Appointment> existing = appointmentRepository
                .findByMainDoctor_StaffIdAndAppointmentDateAndIsDeleted(doctorId, date, 0);
        return existing.stream().anyMatch(a ->
                (excludeAppointmentId == null || !a.getAppointmentId().equals(excludeAppointmentId))
                        && a.getStatus() != AppointmentStatus.CANCELLED
                        && a.getStatus() != AppointmentStatus.NO_SHOW
                        && a.getTimeStart() != null
                        && (a.getTimeStart().equals(slotStart)
                                || (a.getTimeEnd() != null
                                        && a.getTimeStart().isBefore(slotEnd)
                                        && a.getTimeEnd().isAfter(slotStart))));
    }

    private void validateAppointmentLogic(AppointmentRequest request, Integer patientId, Integer doctorId,
            BookingMode mode) {
        boolean isPatientConflict = appointmentRepository
                .existsByPatient_PatientIdAndAppointmentDateAndTimeStartAndIsDeleted(
                        patientId, request.getAppointmentDate(), request.getTimeStart(), 0);
        if (isPatientConflict) {
            throw new RuntimeException("You already have an appointment at this time.");
        }

        if (request.getAppointmentType() == AppointmentType.ONLINE) {
            long spamCount = appointmentRepository
                    .countByPatient_PatientIdAndStatusAndCancelReasonContainingAndIsDeleted(
                            patientId, AppointmentStatus.CANCELLED, "[SPAM]", 0);
            if (spamCount >= 3) {
                throw new RuntimeException(
                        "Account locked due to repeated late cancellations. Please book at clinic.");
            }
            LocalDateTime requestedDateTime = LocalDateTime.of(
                    request.getAppointmentDate(), request.getTimeStart());
            if (requestedDateTime.isBefore(LocalDateTime.now().plusHours(24))) {
                throw new RuntimeException("Online appointments must be booked at least 24 hours in advance.");
            }
        }

        if (doctorId == null) {
            return;
        }

        LocalDate date = request.getAppointmentDate();

        // 1. Weekend check
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new RuntimeException("Phòng khám nghỉ Chủ nhật, vui lòng chọn ngày khác.");
        }

        // 2. Holiday check
        if (HolidayUtils.isHoliday(date)) {
            throw new RuntimeException("Cannot book on holidays.");
        }

        // 3. Leave check
        if (leaveRequestRepository.isDoctorOnLeave(doctorId, date)) {
            throw new RuntimeException("Doctor is on leave on this date.");
        }

        List<StaffSchedule> dailySchedules = scheduleRepository.findByStaff_StaffIdAndWorkingDate(
                doctorId, date);

        // 4. Default schedule check
        if (dailySchedules.isEmpty()) {
            LocalTime start = request.getTimeStart();
            if (start != null) {
                boolean isMorning = (!start.isBefore(LocalTime.of(7, 30)) && start.isBefore(LocalTime.of(11, 30)));
                boolean isAfternoon = (!start.isBefore(LocalTime.of(13, 30)) && start.isBefore(LocalTime.of(17, 0)));
                if (!isMorning && !isAfternoon) {
                    throw new RuntimeException("Requested time is outside default working hours.");
                }
            }
        }

        if (isSlotTakenByDoctor(doctorId, request.getAppointmentDate(),
                request.getTimeStart(), request.getTimeEnd())) {
            throw new RuntimeException("Selected time slot is no longer available.");
        }

        if (request.getAppointmentType() == AppointmentType.WALK_IN && request.getTimeStart() != null) {
            LocalTime latestEndTime;
            if (dailySchedules.isEmpty()) {
                latestEndTime = LocalTime.of(17, 0);
            } else {
                latestEndTime = dailySchedules.stream()
                        .filter(s -> s.getStatus() != ScheduleStatus.OFF)
                        .map(StaffSchedule::getEndTime)
                        .max(LocalTime::compareTo)
                        .orElse(LocalTime.MAX);
            }
            if (request.getTimeStart().plusMinutes(30).isAfter(latestEndTime)) {
                throw new RuntimeException("Doctor is ending shift soon. Cannot accept more walk-ins.");
            }
        }
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        Patient patient = null;
        if (request.getPatientId() != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isStaffOrAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECEPTIONIST") || a.getAuthority().equals("ROLE_NURSE") || a.getAuthority().equals("ROLE_ADMIN"));
            if (isStaffOrAdmin) {
                patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            } else {
                patient = getCurrentPatient();
            }
        } else {
            patient = getCurrentPatient();
        }

        BookingMode mode = resolveBookingMode(request);

        Expertise expertise = loadExpertise(request.getExpertiseId());
        Expertise suggestedExpertise = loadExpertise(request.getSuggestedExpertiseId());
        Staff doctor = null;
        com.clinic.entity.medical.Service service = null;

        switch (mode) {
            case DOCTOR -> {
                if (request.getExpertiseId() == null) {
                    throw new RuntimeException("Vui lòng chọn chuyên khoa.");
                }
                if (request.getMainDoctorId() == null) {
                    throw new RuntimeException("Vui lòng chọn bác sĩ.");
                }
                expertise = loadExpertise(request.getExpertiseId());
                doctor = loadDoctor(request.getMainDoctorId());
                if (doctor.getExpertise() != null
                        && !doctor.getExpertise().getExpertiseId().equals(expertise.getExpertiseId())) {
                    throw new RuntimeException("Bác sĩ không thuộc chuyên khoa đã chọn.");
                }
            }
            case EXPERTISE -> throw new RuntimeException(
                    "Chỉ hỗ trợ 2 luồng đặt lịch: DOCTOR (chọn chuyên khoa + bác sĩ) hoặc SERVICE (xét nghiệm/chụp).");
            case SERVICE -> {
                if (request.getServiceId() == null) {
                    throw new RuntimeException("Service is required for SERVICE booking mode.");
                }
                service = serviceRepository.findById(request.getServiceId())
                        .orElseThrow(() -> new RuntimeException("Service not found"));
                if (service.getServiceType().isHiddenEverywhere()) {
                    throw new RuntimeException(
                            "Dịch vụ khám tổng quát (EXAM) hiện không hỗ trợ đặt lịch.");
                }
                if (!service.getServiceType().isPatientBookable()) {
                    throw new RuntimeException(
                            "Dịch vụ này chỉ được chỉ định trong quá trình khám, không đặt lịch trực tiếp.");
                }
                if (request.getTimeStart() != null) {
                    Integer duration = 30; // Tạm thời fix cứng 30p theo yêu cầu
                    request.setTimeEnd(request.getTimeStart().plusMinutes(duration));
                    doctor = autoAssignTechnician(request.getAppointmentDate(), request.getTimeStart(), duration);
                    request.setMainDoctorId(doctor.getStaffId());
                }
            }
            default -> throw new RuntimeException("Unsupported booking mode.");
        }

        validateAppointmentLogic(request, patient.getPatientId(), request.getMainDoctorId(), mode);

        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setPatient(patient);
        appointment.setMainDoctor(doctor);
        appointment.setService(service);
        appointment.setExpertise(expertise);
        appointment.setSuggestedExpertise(suggestedExpertise);
        appointment.setBookingMode(mode);
        appointment.setIsAiSuggested(Boolean.TRUE.equals(request.getIsAiSuggested()));
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setIsDeleted(0);

        if (request.getAppointmentType() == AppointmentType.WALK_IN) {
            appointment.setStatus(AppointmentStatus.CHECKED_IN);
            appointment.setCheckinTime(LocalDateTime.now());
            
            // Check if this is a priority walk-in (e.g. Emergency, VIP)
            if (Boolean.TRUE.equals(request.getIsPriority())) {
                appointment.setQueueNumber(0);
            } else {
                Integer maxQueue = appointmentRepository.findMaxQueueNumberByDate(request.getAppointmentDate()).orElse(0);
                appointment.setQueueNumber(maxQueue + 1);
            }
            if (mode == BookingMode.SERVICE) {
                // Tự động chuyển qua phòng Lab
                appointment.setStatus(AppointmentStatus.WAITING_RESULT);
            }
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Auto-create medical record and service order for SERVICE mode walk-in
        if (request.getAppointmentType() == AppointmentType.WALK_IN && mode == BookingMode.SERVICE && service != null) {
            MedicalRecord record = new MedicalRecord();
            record.setPatient(patient);
            record.setAppointment(savedAppointment);
            record.setStatus(MedicalRecordStatus.WAITING_RESULT);
            MedicalRecord savedRecord = medicalRecordRepository.save(record);

            ServiceOrder order = new ServiceOrder();
            order.setMedicalRecord(savedRecord);
            order.setService(service);
            // Giá dịch vụ (ưu tiên giá khuyến mãi nếu có)
            order.setServiceOriginalFee(service.getOriginalPrice());
            order.setServiceDiscount(service.getDiscountAmount());
            order.setServiceFinalFee(service.getDiscountAmount() != null && service.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0 ? service.getDiscountAmount() : service.getOriginalPrice());
            order.setStatus(ServiceOrderStatus.ORDERED);
            // System/Receptionist ordered it
            if (request.getCreatedBy() != null && request.getCreatedBy().equals("RECEPTIONIST") || request.getCreatedBy().equals("NURSE")) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName() != null) {
                    staffRepository.findByAccount_Email(auth.getName()).ifPresent(order::setOrderedBy);
                }
            }
            serviceOrderRepository.save(order);
        } else if (request.getAppointmentType() == AppointmentType.WALK_IN && mode == BookingMode.DOCTOR) {
            MedicalRecord record = new MedicalRecord();
            record.setPatient(patient);
            record.setAppointment(savedAppointment);
            record.setMainDoctor(savedAppointment.getMainDoctor());
            record.setStatus(com.clinic.common.enums.MedicalRecordStatus.PENDING);
            record.setVitalsTaken(false);
            medicalRecordRepository.save(record);
        }

        // Send Notification
        String dateStr = request.getAppointmentDate().toString();
        String timeStr = request.getTimeStart().toString();
        if (patient.getAccount() != null) {
            if (request.getAppointmentType() == AppointmentType.WALK_IN) {
                if (mode == BookingMode.SERVICE) {
                    notificationService.createAndSendNotification(
                            patient.getAccount().getAccountId(),
                            "Bạn đã được tạo phiếu chỉ định. Vui lòng di chuyển đến phòng Xét nghiệm / Chụp chiếu.",
                            "SYSTEM");
                } else {
                    notificationService.createAndSendNotification(
                            patient.getAccount().getAccountId(),
                            "Lịch khám trực tiếp của bạn đã được Check-in. Số thứ tự của bạn là " + savedAppointment.getQueueNumber() + ".",
                            "SYSTEM");
                }
            } else {
                notificationService.createAndSendNotification(
                        patient.getAccount().getAccountId(),
                        "Lịch hẹn khám của bạn vào ngày " + dateStr + " lúc " + timeStr + " đã được tạo thành công.",
                        "SYSTEM");
            }
        }

        return enrichResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponse updateAppointment(Integer id, com.clinic.dto.appointment.AppointmentRescheduleRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // 1. Status Validation
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new RuntimeException("Không thể dời lịch khi trạng thái là " + appointment.getStatus().name());
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isPatient = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));

        // 2. Limit Check for Patient
        int currentCount = appointment.getRescheduleCount() != null ? appointment.getRescheduleCount() : 0;
        if (isPatient) {
            if (currentCount >= 2) {
                throw new RuntimeException("Lịch hẹn này đã đạt giới hạn số lần dời lịch (2 lần). Vui lòng liên hệ lễ tân để được hỗ trợ.");
            }
        }
        
        // 3. Time constraint for PATIENT
        if (isPatient) {
            if (appointment.getAppointmentDate() != null && appointment.getTimeStart() != null) {
                LocalDateTime appointmentDateTime = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getTimeStart());
                if (LocalDateTime.now().isAfter(appointmentDateTime.minusHours(3))) {
                    throw new RuntimeException("Chỉ được phép dời lịch trước thời gian khám hiện tại ít nhất 3 tiếng.");
                }
            }
        }

        LocalDate oldDate = appointment.getAppointmentDate();
        LocalDate newDate = request.getAppointmentDate();
        
        // 4. Validate new slot
        if (request.getMainDoctorId() != null || appointment.getMainDoctor() != null) {
            Integer doctorId = request.getMainDoctorId() != null ? request.getMainDoctorId() : appointment.getMainDoctor().getStaffId();
            
            if (newDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY && !newDate.equals(LocalDate.now())) {
                throw new RuntimeException("Phòng khám nghỉ Chủ nhật, vui lòng chọn ngày khác.");
            }
            if (HolidayUtils.isHoliday(newDate)) {
                throw new RuntimeException("Không thể đặt lịch vào ngày nghỉ lễ.");
            }
            if (leaveRequestRepository.isDoctorOnLeave(doctorId, newDate)) {
                throw new RuntimeException("Bác sĩ có lịch nghỉ vào ngày này.");
            }
            
            List<com.clinic.entity.staff.StaffSchedule> dailySchedules = scheduleRepository.findByStaff_StaffIdAndWorkingDate(doctorId, newDate);
            if (dailySchedules.isEmpty()) {
                LocalTime start = request.getTimeStart();
                if (start != null) {
                    boolean isMorning = (!start.isBefore(LocalTime.of(7, 30)) && start.isBefore(LocalTime.of(11, 30)));
                    boolean isAfternoon = (!start.isBefore(LocalTime.of(13, 30)) && start.isBefore(LocalTime.of(17, 0)));
                    if (!isMorning && !isAfternoon) {
                        throw new RuntimeException("Giờ khám nằm ngoài giờ làm việc mặc định.");
                    }
                }
            }

            if (isSlotTakenByDoctor(doctorId, newDate, request.getTimeStart(), request.getTimeEnd(), id)) {
                throw new RuntimeException("Khung giờ này đã có người đặt, vui lòng chọn giờ khác.");
            }
        }
        
        // 5. Update data
        appointment.setAppointmentDate(newDate);
        appointment.setTimeStart(request.getTimeStart());
        appointment.setTimeEnd(request.getTimeEnd());
        appointment.setRescheduleReason(request.getRescheduleReason());
        if (isPatient) {
            appointment.setRescheduleCount(currentCount + 1);
        }
        
        if (request.getMainDoctorId() != null) {
            Staff doctor = staffRepository.findById(request.getMainDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
            appointment.setMainDoctor(doctor);
        }

        if (appointment.getStatus() == AppointmentStatus.CHECKED_IN) {
            appointment.setStatus(AppointmentStatus.PENDING);
            appointment.setCheckinTime(null);
            appointment.setQueueNumber(null);
            
            medicalRecordRepository.findByAppointment_AppointmentId(appointment.getAppointmentId()).ifPresent(record -> {
                if (record.getStatus() == com.clinic.common.enums.MedicalRecordStatus.PENDING) {
                    record.setStatus(com.clinic.common.enums.MedicalRecordStatus.CANCELLED);
                    medicalRecordRepository.save(record);
                }
            });
        }
        
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 6. Notify Patient
        if (!isPatient) {
            if (appointment.getPatient() != null && appointment.getPatient().getAccount() != null) {
                notificationService.createAndSendNotification(
                        appointment.getPatient().getAccount().getAccountId(),
                        "Lịch hẹn khám của bạn đã được dời sang ngày " + newDate + " lúc " + request.getTimeStart() + ".",
                        "SYSTEM");
            }
        }

        return enrichResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponse updateStatus(Integer id, AppointmentStatus newStatus, Boolean isPriority) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        boolean justCheckedIn = false;
        boolean justCompleted = false;
        boolean justConfirmed = false;
        boolean justCancelled = false;

        if (newStatus == AppointmentStatus.CONFIRMED && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            justConfirmed = true;
        }
        if (newStatus == AppointmentStatus.CANCELLED && appointment.getStatus() != AppointmentStatus.CANCELLED) {
            justCancelled = true;
        }

        if (newStatus == AppointmentStatus.CHECKED_IN) {
            if (appointment.getStatus() == AppointmentStatus.CHECKED_IN) {
                throw new RuntimeException("Lịch hẹn đã được Check-in trước đây.");
            }
            if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
                throw new RuntimeException("Chỉ có thể Check-in lịch hẹn ở trạng thái Chờ khám hoặc Đã xác nhận.");
            }
            appointment.setCheckinTime(LocalDateTime.now());
            
            if (Boolean.TRUE.equals(isPriority)) {
                appointment.setQueueNumber(0);
            } else {
                Integer maxQueue = appointmentRepository.findMaxQueueNumberByDateExclude(appointment.getAppointmentDate(), appointment.getAppointmentId()).orElse(0);
                appointment.setQueueNumber(maxQueue + 1);
            }
            
            justCheckedIn = true;
            
            // Auto-create Service Order for ONLINE/APP SERVICE mode when checking in
            if (appointment.getBookingMode() == BookingMode.SERVICE && appointment.getService() != null) {
                newStatus = AppointmentStatus.WAITING_RESULT;
                
                MedicalRecord record = new MedicalRecord();
                record.setPatient(appointment.getPatient());
                record.setAppointment(appointment);
                record.setStatus(com.clinic.common.enums.MedicalRecordStatus.WAITING_RESULT);
                MedicalRecord savedRecord = medicalRecordRepository.save(record);

                ServiceOrder order = new ServiceOrder();
                order.setMedicalRecord(savedRecord);
                order.setService(appointment.getService());
                order.setServiceOriginalFee(appointment.getService().getOriginalPrice());
                order.setServiceDiscount(appointment.getService().getDiscountAmount());
                order.setServiceFinalFee(appointment.getService().getDiscountAmount() != null && appointment.getService().getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0 ? appointment.getService().getDiscountAmount() : appointment.getService().getOriginalPrice());
                order.setStatus(ServiceOrderStatus.ORDERED);
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName() != null) {
                    staffRepository.findByAccount_Email(auth.getName()).ifPresent(order::setOrderedBy);
                }
                serviceOrderRepository.save(order);
            } else if (appointment.getBookingMode() == BookingMode.DOCTOR) {
                if (medicalRecordRepository.findByAppointment_AppointmentId(appointment.getAppointmentId()).isEmpty()) {
                    MedicalRecord record = new MedicalRecord();
                    record.setPatient(appointment.getPatient());
                    record.setAppointment(appointment);
                    record.setMainDoctor(appointment.getMainDoctor());
                    record.setStatus(com.clinic.common.enums.MedicalRecordStatus.PENDING);
                    record.setVitalsTaken(false);
                    medicalRecordRepository.save(record);
                }
            }
        }
        if (newStatus == AppointmentStatus.COMPLETED && appointment.getStatus() != AppointmentStatus.COMPLETED) {
            appointment.setCheckoutTime(LocalDateTime.now());
            justCompleted = true;
        }
        appointment.setStatus(newStatus);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);

        if (justCheckedIn) {
            if (appointment.getPatient().getAccount() != null) {
                if (appointment.getBookingMode() == BookingMode.SERVICE) {
                    notificationService.createAndSendNotification(
                            appointment.getPatient().getAccount().getAccountId(),
                            "Bạn đã được tạo phiếu chỉ định. Vui lòng di chuyển đến phòng Xét nghiệm / Chụp chiếu.",
                            "SYSTEM");
                } else {
                    notificationService.createAndSendNotification(
                            appointment.getPatient().getAccount().getAccountId(),
                            "Bạn đã check-in thành công. Vui lòng chờ đến lượt khám. Số thứ tự của bạn là " + savedAppointment.getQueueNumber() + ".",
                            "SYSTEM");
                }
            }
        } else if (justCompleted) {
            if (appointment.getPatient().getAccount() != null) {
                notificationService.createAndSendNotification(
                        appointment.getPatient().getAccount().getAccountId(),
                        "Ca khám của bạn đã hoàn tất. Cảm ơn bạn đã sử dụng dịch vụ của phòng khám.",
                        "SYSTEM");
            }
        } else if (justConfirmed) {
            if (appointment.getPatient().getAccount() != null) {
                notificationService.createAndSendNotification(
                        appointment.getPatient().getAccount().getAccountId(),
                        "Lịch hẹn khám ngày " + appointment.getAppointmentDate() + " lúc " + appointment.getTimeStart() + " của bạn đã được phê duyệt thành công.",
                        "SYSTEM");
            }
        } else if (justCancelled) {
            if (appointment.getPatient().getAccount() != null) {
                notificationService.createAndSendNotification(
                        appointment.getPatient().getAccount().getAccountId(),
                        "Lịch hẹn khám ngày " + appointment.getAppointmentDate() + " lúc " + appointment.getTimeStart() + " của bạn đã bị hủy bởi phòng khám.",
                        "SYSTEM");
            }
        }

        return enrichResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponse cancelByPatient(Integer id, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập lý do hủy lịch.");
        }
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new RuntimeException("Không thể hủy lịch khi trạng thái là " + appointment.getStatus().name());
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isPatient = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
        
        if (isPatient) {
            LocalDateTime appointmentDateTime = LocalDateTime.of(
                    appointment.getAppointmentDate(), appointment.getTimeStart());
            if (LocalDateTime.now().isAfter(appointmentDateTime.minusHours(3))) {
                throw new RuntimeException("Chỉ được phép hủy lịch trước thời gian khám ít nhất 3 tiếng.");
            }
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledBy(CancelledByType.PATIENT);
        appointment.setCancelReason(reason);
        appointment.setIsDeleted(1);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);

        notificationService.createAndSendNotification(
                appointment.getPatient().getAccount().getAccountId(),
                "Lịch hẹn khám ngày " + appointment.getAppointmentDate() + " lúc " + appointment.getTimeStart() + " đã bị hủy với lý do: " + reason,
                "SYSTEM");

        return enrichResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponse transferDoctor(Integer appointmentId, Integer newDoctorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        Staff newDoctor = staffRepository.findById(newDoctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        appointment.setMainDoctor(newDoctor);
        if (appointment.getExpertise() == null && newDoctor.getExpertise() != null) {
            appointment.setExpertise(newDoctor.getExpertise());
        }
        
        Appointment savedAppointment = appointmentRepository.save(appointment);

        if (appointment.getPatient().getAccount() != null) {
            notificationService.createAndSendNotification(
                    appointment.getPatient().getAccount().getAccountId(),
                    "Lịch hẹn của bạn đã được chuyển sang Bác sĩ " + newDoctor.getFullName() + " phụ trách.",
                    "SYSTEM");
        }

        return enrichResponse(savedAppointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllActive() {
        return appointmentRepository.findByIsDeleted(0).stream()
                .map(this::enrichResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments() {
        Patient patient = getCurrentPatient();
        return appointmentRepository.findByPatient_PatientIdAndIsDeletedOrderByAppointmentDateDesc(
                        patient.getPatientId(), 0)
                .stream()
                .map(this::enrichResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getDetail(Integer id) {
        Patient patient = getCurrentPatient();
        Appointment appointment = appointmentRepository.findByAppointmentIdAndIsDeleted(id, 0)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (!appointment.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new RuntimeException("Access denied");
        }
        return enrichResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableSlots(
            Integer doctorId, Integer expertiseId, Integer serviceId, LocalDate date) {
        if (doctorId != null) {
            return buildSlotsForDoctor(doctorId, date, null, 30);
        }
        if (expertiseId != null) {
            return buildSlotsForExpertise(expertiseId, date, 30);
        }
        if (serviceId != null) {
            com.clinic.entity.medical.Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("Service not found"));
            if (service.getServiceType() == ServiceType.EXAM) {
                return buildSlotsForExpertise(null, date, 30);
            }
            Integer duration = service.getEstimatedDuration() != null ? service.getEstimatedDuration() : 15;
            return buildSlotsForLabStaff(date, duration);
        }
        return List.of();
    }

    private List<TimeSlotResponse> buildSlotsForExpertise(Integer expertiseId, LocalDate date, Integer intervalMinutes) {
        List<Staff> doctors = expertiseId != null
                ? staffRepository.findByExpertise_ExpertiseIdAndStaffTypeAndIsDeleted(
                        expertiseId, StaffType.DOCTOR, 0)
                : staffRepository.findByStaffTypeAndIsDeleted(StaffType.DOCTOR, 0);

        Map<String, TimeSlotResponse> merged = new LinkedHashMap<>();
        for (Staff doctor : doctors) {
            for (TimeSlotResponse slot : buildSlotsForDoctor(doctor.getStaffId(), date, doctor, intervalMinutes)) {
                if (!slot.isAvailable()) {
                    continue;
                }
                String key = slot.getTimeStart() + "-" + slot.getTimeEnd();
                merged.putIfAbsent(key, slot);
            }
        }
        return merged.values().stream()
                .sorted(Comparator.comparing(TimeSlotResponse::getTimeStart))
                .collect(Collectors.toList());
    }

    private List<TimeSlotResponse> buildSlotsForLabStaff(LocalDate date, Integer intervalMinutes) {
        List<Staff> labStaff = staffRepository.findByStaffTypeAndIsDeleted(StaffType.LAB_TECH, 0);
        if (labStaff.isEmpty()) {
            labStaff = staffRepository.findByStaffTypeAndIsDeleted(StaffType.NURSE, 0);
        }
        Map<String, TimeSlotResponse> merged = new LinkedHashMap<>();
        for (Staff staff : labStaff) {
            for (TimeSlotResponse slot : buildSlotsForDoctor(staff.getStaffId(), date, staff, intervalMinutes)) {
                if (!slot.isAvailable()) {
                    continue;
                }
                String key = slot.getTimeStart() + "-" + slot.getTimeEnd();
                merged.putIfAbsent(key, slot);
            }
        }
        return merged.values().stream()
                .sorted(Comparator.comparing(TimeSlotResponse::getTimeStart))
                .collect(Collectors.toList());
    }

    private List<TimeSlotResponse> buildSlotsForDoctor(Integer doctorId, LocalDate date, Staff staffRef, Integer intervalMinutes) {
        List<TimeSlotResponse> slots = new ArrayList<>();
        
        // 1. Check weekends
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return slots;
        }
        
        // 2. Check holidays
        if (HolidayUtils.isHoliday(date)) {
            return slots;
        }

        // 3. Check leaves
        if (leaveRequestRepository.isDoctorOnLeave(doctorId, date)) {
            return slots;
        }

        List<StaffSchedule> schedules = scheduleRepository.findByStaff_StaffIdAndWorkingDate(doctorId, date);
        Staff doctor = staffRef != null ? staffRef : staffRepository.findById(doctorId).orElse(null);

        // 4. Default schedules if not specifically set
        if (schedules.isEmpty()) {
            return generateDefaultSlots(doctorId, date, doctor, intervalMinutes);
        }

        for (StaffSchedule schedule : schedules) {
            if (schedule.getStatus() == ScheduleStatus.OFF) {
                continue;
            }
            LocalTime current = schedule.getStartTime();
            LocalTime end = schedule.getEndTime();
            while (current.plusMinutes(intervalMinutes).isBefore(end) || current.plusMinutes(intervalMinutes).equals(end)) {
                LocalTime slotStart = current;
                LocalTime slotEnd = current.plusMinutes(intervalMinutes);
                boolean available = !isSlotTakenByDoctor(doctorId, date, slotStart, slotEnd);
                slots.add(TimeSlotResponse.builder()
                        .timeStart(slotStart)
                        .timeEnd(slotEnd)
                        .isAvailable(available)
                        .doctorId(doctor != null ? doctor.getStaffId() : doctorId)
                        .doctorName(doctor != null ? doctor.getFullName() : null)
                        .build());
                current = current.plusMinutes(intervalMinutes);
            }
        }
        return slots;
    }

    private List<TimeSlotResponse> generateDefaultSlots(Integer doctorId, LocalDate date, Staff doctor, Integer intervalMinutes) {
        List<TimeSlotResponse> slots = new ArrayList<>();
        generateSlotsForRange(doctorId, date, doctor, LocalTime.of(7, 30), LocalTime.of(11, 30), slots, intervalMinutes);
        generateSlotsForRange(doctorId, date, doctor, LocalTime.of(13, 30), LocalTime.of(17, 0), slots, intervalMinutes);
        return slots;
    }

    private void generateSlotsForRange(Integer doctorId, LocalDate date, Staff doctor, LocalTime start, LocalTime end, List<TimeSlotResponse> slots, Integer intervalMinutes) {
        LocalTime current = start;
        while (current.plusMinutes(intervalMinutes).isBefore(end) || current.plusMinutes(intervalMinutes).equals(end)) {
            LocalTime slotStart = current;
            LocalTime slotEnd = current.plusMinutes(intervalMinutes);
            boolean available = !isSlotTakenByDoctor(doctorId, date, slotStart, slotEnd);
            slots.add(TimeSlotResponse.builder()
                    .timeStart(slotStart)
                    .timeEnd(slotEnd)
                    .isAvailable(available)
                    .doctorId(doctor != null ? doctor.getStaffId() : doctorId)
                    .doctorName(doctor != null ? doctor.getFullName() : null)
                    .build());
            current = current.plusMinutes(intervalMinutes);
        }
    }
}
