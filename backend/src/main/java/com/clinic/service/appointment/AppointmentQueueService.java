package com.clinic.service.appointment;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.dto.appointment.AppointmentResponse;
import com.clinic.entity.appointment.Appointment;
import com.clinic.mapper.appointment.AppointmentMapper;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.service.crm.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AppointmentQueueService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final NotificationService notificationService;

    /**
     * Bác sĩ gọi bệnh nhân vào khám (CHECKED_IN -> IN_PROGRESS)
     * Hoặc gọi lại bệnh nhân (SKIPPED -> IN_PROGRESS)
     */
    @Transactional
    public AppointmentResponse callPatient(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN && 
            appointment.getStatus() != AppointmentStatus.SKIPPED) {
            throw new RuntimeException("Chỉ có thể gọi khám bệnh nhân đang ở trạng thái CHECKED_IN hoặc SKIPPED.");
        }

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Gửi thông báo (tùy chọn, ví dụ hiển thị lên màn hình TV ở phòng khám)
        notificationService.createAndSendNotification(
                appointment.getPatient().getAccount().getAccountId(),
                "Đến lượt khám của bạn. Vui lòng vào phòng khám gặp Bác sĩ " + 
                (appointment.getMainDoctor() != null ? appointment.getMainDoctor().getFullName() : ""),
                "SYSTEM");

        return appointmentMapper.toResponse(savedAppointment);
    }

    /**
     * Bác sĩ gọi nhưng bệnh nhân không có mặt (IN_PROGRESS/CHECKED_IN -> SKIPPED)
     */
    @Transactional
    public AppointmentResponse skipPatient(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN && 
            appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new RuntimeException("Chỉ có thể bỏ qua bệnh nhân đang ở trạng thái CHECKED_IN hoặc IN_PROGRESS.");
        }

        appointment.setStatus(AppointmentStatus.SKIPPED);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        notificationService.createAndSendNotification(
                appointment.getPatient().getAccount().getAccountId(),
                "Bạn đã bị qua lượt do không có mặt khi bác sĩ gọi tên. Vui lòng liên hệ quầy lễ tân để được xếp lại vào cuối hàng đợi.",
                "SYSTEM");

        return appointmentMapper.toResponse(savedAppointment);
    }

    /**
     * Bệnh nhân lỡ lượt (SKIPPED) quay lại báo lễ tân -> Đưa lại vào hàng đợi (CHECKED_IN)
     */
    @Transactional
    public AppointmentResponse returnToQueue(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.SKIPPED) {
            throw new RuntimeException("Chỉ có thể xếp lại hàng đợi cho bệnh nhân bị qua lượt (SKIPPED).");
        }

        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        // Có thể cấp lại số thứ tự mới ở cuối hàng hoặc giữ nguyên số cũ nhưng chèn vào sau.
        // Ở đây ta giữ nguyên số cũ, hệ thống UI sẽ tự sort theo trạng thái CHECKED_IN.
        
        Appointment savedAppointment = appointmentRepository.save(appointment);

        notificationService.createAndSendNotification(
                appointment.getPatient().getAccount().getAccountId(),
                "Bạn đã được xếp lại vào hàng đợi. Vui lòng chú ý theo dõi thông báo.",
                "SYSTEM");

        return appointmentMapper.toResponse(savedAppointment);
    }

    /**
     * Bác sĩ chỉ định đi xét nghiệm (IN_PROGRESS -> WAITING_RESULT)
     * Giải phóng phòng khám cho bệnh nhân tiếp theo.
     */
    @Transactional
    public AppointmentResponse sendToLab(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new RuntimeException("Bệnh nhân phải đang khám (IN_PROGRESS) mới có thể chuyển sang chờ kết quả.");
        }

        appointment.setStatus(AppointmentStatus.WAITING_RESULT);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        notificationService.createAndSendNotification(
                appointment.getPatient().getAccount().getAccountId(),
                "Bác sĩ đã chỉ định cận lâm sàng. Vui lòng di chuyển đến khu vực xét nghiệm/chụp chiếu.",
                "SYSTEM");

        return appointmentMapper.toResponse(savedAppointment);
    }

    /**
     * Bệnh nhân có kết quả xét nghiệm quay lại phòng khám (WAITING_RESULT -> CHECKED_IN nhưng ưu tiên)
     * Luồng Re-exam (Đọc kết quả)
     */
    @Transactional
    public AppointmentResponse returnFromLab(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.WAITING_RESULT) {
            throw new RuntimeException("Bệnh nhân phải đang ở trạng thái chờ kết quả (WAITING_RESULT).");
        }

        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        // Đặt số thứ tự thành số âm hoặc 0 để luôn trồi lên đầu danh sách của bác sĩ
        appointment.setQueueNumber(0); 
        
        Appointment savedAppointment = appointmentRepository.save(appointment);

        notificationService.createAndSendNotification(
                appointment.getPatient().getAccount().getAccountId(),
                "Đã có kết quả xét nghiệm. Bạn đã được xếp ưu tiên vào gặp bác sĩ để đọc kết quả.",
                "SYSTEM");

        return appointmentMapper.toResponse(savedAppointment);
    }
}
